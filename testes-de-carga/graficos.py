"""
Gera os numeros e os graficos das medicoes do SLA e escreve tudo no README.md desta pasta.
Inspirado no plot_k6.py do exemplo da monitoria (Simulacao-Teste-de-Carga-PI2-2025-01).

Uso (na pasta testes-de-carga, depois de rodar os testes; o rodar-testes.bat ja chama):
    python graficos.py        -> medicao 1
    python graficos.py 2      -> medicao 2 (e os graficos comparando com a medicao 1)

Le    resultados/medicao-N/bruto/<servico>.csv   (cada medicao do k6, gravada com --out csv)
Gera  resultados/medicao-N/<servico>-latencia.png  latencia media por segundo, com P95, P99 e os VUs
      resultados/medicao-N/<servico>-vazao.png     requisicoes por segundo, com os VUs
      resultados/medicao-N/<servico>-erros.png     % de erros por segundo, com os VUs
      resultados/medicao-N/<servico>-bloqueio.png  tempo bloqueado (http_req_blocked), com os VUs
      resultados/medicao-N/comparacao-servicos.png os servicos lado a lado em cada patamar de VUs
      resultados/medicao-N/resumo.json             os numeros de cada servico e patamar
      resultados/comparacao-<servico>.png          medicao 1 x medicao 2 (so quando as duas existem)
e preenche as partes marcadas com <!-- ... --> no README.md.
"""
import csv
import json
import os
import re
import sys
from collections import defaultdict
from datetime import datetime

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

AQUI = os.path.dirname(os.path.abspath(__file__))
README = os.path.join(AQUI, "README.md")
SERVICOS = [
    ("login", "Login"),
    ("eventos", "Agenda de eventos"),
    ("presenca", "Confirmar presença"),
    ("feed", "Feed"),  # incluido na medicao 2
]
PATAMARES = [10, 50, 100, 200, 500]
SLA_P95_MS = 2000  # RNF002: responder em ate 2 s
SLA_ERRO = 1.0  # RNF003: estabilidade, menos de 1% de erros
CORES = {"login": "#1f77b4", "eventos": "#2ca02c", "presenca": "#d62728", "feed": "#9467bd"}


# ---------- Numeros ----------

def percentil(valores, p):
    ordenados = sorted(valores)
    if not ordenados:
        return 0.0
    k = (len(ordenados) - 1) * p / 100
    baixo = int(k)
    alto = min(baixo + 1, len(ordenados) - 1)
    return ordenados[baixo] + (ordenados[alto] - ordenados[baixo]) * (k - baixo)


def estatisticas(valores):
    if not valores:
        return None
    return {
        "media": sum(valores) / len(valores),
        "mediana": percentil(valores, 50),
        "min": min(valores),
        "max": max(valores),
        "p95": percentil(valores, 95),
        "p99": percentil(valores, 99),
    }


def ler_csv(caminho):
    """Devolve as medicoes do k6 agrupadas por metrica: [(segundo, valor, fase)]."""
    dados = defaultdict(list)
    with open(caminho, encoding="utf-8") as f:
        for linha in csv.DictReader(f):
            nome = linha["metric_name"]
            if nome not in ("http_req_duration", "http_req_blocked", "http_req_failed", "http_reqs", "vus"):
                continue
            fase = ""
            m = re.search(r"fase=([\w]+)", linha.get("extra_tags") or "")
            if m:
                fase = m.group(1)
            dados[nome].append((int(float(linha["timestamp"])), float(linha["metric_value"]), fase))
    inicio = min(t for lista in dados.values() for t, _, _ in lista)
    relativos = {nome: [(t - inicio, v, f) for t, v, f in lista] for nome, lista in dados.items()}
    return relativos, datetime.fromtimestamp(inicio)


def resumir(dados):
    duracao = [v for _, v, _ in dados["http_req_duration"]]
    bloqueio = [v for _, v, _ in dados.get("http_req_blocked", [])]
    resumo = {
        "requisicoes": len(dados["http_reqs"]),
        "duracao": estatisticas(duracao),
        "bloqueio": estatisticas(bloqueio),
        "patamares": [],
    }
    for vus in PATAMARES:
        fase = str(vus)
        d = [v for _, v, f in dados["http_req_duration"] if f == fase]
        falhas = [v for _, v, f in dados["http_req_failed"] if f == fase]
        segundos = {t for t, _, f in dados["http_reqs"] if f == fase}
        if not d:
            continue
        tempo = (max(segundos) - min(segundos) + 1) if segundos else 1
        resumo["patamares"].append({
            "vus": vus,
            "latencia_media": sum(d) / len(d),
            "p95": percentil(d, 95),
            "vazao": len(d) / tempo,
            "erro": 100 * sum(falhas) / len(falhas) if falhas else 0.0,
            "requisicoes": len(d),
        })
    dentro = [p["vus"] for p in resumo["patamares"] if p["p95"] < SLA_P95_MS and p["erro"] < SLA_ERRO]
    # o limite e o maior patamar em que o SLA ainda vale, desde que todos os anteriores tambem valham
    limite = None
    for p in resumo["patamares"]:
        if p["vus"] in dentro:
            limite = p["vus"]
        else:
            break
    resumo["limite"] = limite
    resumo["primeiro_fora"] = next((p["vus"] for p in resumo["patamares"] if p["vus"] not in dentro), None)
    return resumo


# ---------- Graficos ----------

def por_segundo(pontos, modo):
    grupos = defaultdict(list)
    for t, v, _ in pontos:
        grupos[t].append(v)
    xs = sorted(grupos)
    if modo == "soma":
        return xs, [sum(grupos[x]) for x in xs]
    if modo == "porcento":
        return xs, [100 * sum(grupos[x]) / len(grupos[x]) for x in xs]
    return xs, [sum(grupos[x]) / len(grupos[x]) for x in xs]


def grafico_no_tempo(dados, metrica, modo, rotulo_y, titulo, cor, caminho, percentis=False):
    pontos = dados.get(metrica)
    if not pontos:
        return
    x, y = por_segundo(pontos, modo)
    fig, ax1 = plt.subplots(figsize=(14, 6))
    ax1.plot(x, y, color=cor, linewidth=2, label=rotulo_y)
    if percentis:
        valores = [v for _, v, _ in pontos]
        ax1.axhline(percentil(valores, 95), color="goldenrod", linestyle="--", linewidth=1.5, label="P95")
        ax1.axhline(percentil(valores, 99), color="red", linestyle=":", linewidth=1.5, label="P99")
    if modo == "porcento":
        # erros: eixo de 0 a 100%, para nao aparecer escala negativa quando nao ha erro
        ax1.set_ylim(0, 100)
    else:
        ax1.set_ylim(bottom=0)
    ax1.set_xlabel("Tempo (s)", fontsize=13)
    ax1.set_ylabel(rotulo_y, fontsize=13, color=cor)
    ax1.grid(True, linestyle="--", alpha=0.6)
    linhas, rotulos = ax1.get_legend_handles_labels()
    if dados.get("vus"):
        vx, vy = por_segundo(dados["vus"], "media")
        ax2 = ax1.twinx()
        ax2.fill_between(vx, vy, color="gray", alpha=0.2, label="Concorrência (VUs)")
        ax2.set_ylabel("Concorrência (VUs)", fontsize=13, color="gray")
        l2, r2 = ax2.get_legend_handles_labels()
        linhas, rotulos = linhas + l2, rotulos + r2
    ax1.legend(linhas, rotulos, fontsize=11, loc="upper left")
    plt.title(titulo, fontsize=15, fontweight="bold")
    plt.tight_layout()
    plt.savefig(caminho)
    plt.close(fig)


def grafico_servicos(resumos, titulo, caminho):
    """Os servicos lado a lado: latencia, vazao e erros em cada patamar de VUs."""
    fig, eixos = plt.subplots(1, 3, figsize=(17, 5))
    metricas = [("latencia_media", "Latência média (ms)"), ("vazao", "Vazão (req/s)"), ("erro", "Erros (%)")]
    for ax, (chave, nome) in zip(eixos, metricas):
        for servico, rotulo in SERVICOS:
            r = resumos.get(servico)
            if not r:
                continue
            xs = [p["vus"] for p in r["patamares"]]
            ax.plot(xs, [p[chave] for p in r["patamares"]], marker="o", linewidth=2, color=CORES[servico], label=rotulo)
        ax.set_title(nome, fontsize=13, fontweight="bold")
        ax.set_xlabel("Usuários virtuais (VUs)")
        ax.set_xticks(PATAMARES)
        ax.grid(True, linestyle="--", alpha=0.6)
    eixos[0].legend()
    plt.suptitle(titulo, fontsize=15, fontweight="bold")
    plt.tight_layout()
    plt.savefig(caminho)
    plt.close(fig)


def grafico_medicoes(servico, rotulo, r1, r2, caminho):
    """Medicao 1 x medicao 2 de um servico, em cada patamar de VUs."""
    fig, eixos = plt.subplots(1, 3, figsize=(17, 5))
    metricas = [("latencia_media", "Latência média (ms)"), ("vazao", "Vazão (req/s)"), ("erro", "Erros (%)")]
    largura = 0.38
    for ax, (chave, nome) in zip(eixos, metricas):
        v1 = {p["vus"]: p[chave] for p in r1["patamares"]}
        v2 = {p["vus"]: p[chave] for p in r2["patamares"]}
        xs = range(len(PATAMARES))
        b1 = ax.bar([x - largura / 2 for x in xs], [v1.get(v, 0) for v in PATAMARES], largura, label="Medição 1", color="#9aa5b1")
        b2 = ax.bar([x + largura / 2 for x in xs], [v2.get(v, 0) for v in PATAMARES], largura, label="Medição 2", color=CORES[servico])
        ax.bar_label(b1, fmt="%.1f", fontsize=8)
        ax.bar_label(b2, fmt="%.1f", fontsize=8)
        ax.set_xticks(list(xs), [f"{v} VUs" for v in PATAMARES])
        ax.set_title(nome, fontsize=13, fontweight="bold")
        ax.grid(True, axis="y", linestyle="--", alpha=0.6)
    eixos[0].legend()
    plt.suptitle(f"{rotulo}: medição 1 x medição 2", fontsize=15, fontweight="bold")
    plt.tight_layout()
    plt.savefig(caminho)
    plt.close(fig)


# ---------- Texto para o README ----------

def texto_medicao(servico, rotulo, r, data, medicao):
    pasta = f"resultados/medicao-{medicao}"
    d, b = r["duracao"], r["bloqueio"]
    if r["limite"] is None:
        limite = f"o SLA já não vale no primeiro patamar ({PATAMARES[0]} VUs)"
    elif r["primeiro_fora"] is None:
        limite = f"o SLA valeu em todos os patamares, até **{r['limite']} VUs** (o limite real fica acima disso)"
    else:
        limite = f"**{r['limite']} VUs**; com {r['primeiro_fora']} VUs o SLA deixa de valer"
    linhas = [
        f"- **Data da medição:** {data:%d/%m/%Y}",
        f"- **Requisições no teste:** {r['requisicoes']}",
        f"- **Limite de concorrência dentro do SLA** (p95 < {SLA_P95_MS} ms e erros < {SLA_ERRO:g}%): {limite}",
        "",
        "| Concorrência (VUs) | Latência média (ms) | p95 (ms) | Vazão (req/s) | Erros (%) | SLA |",
        "| --- | --- | --- | --- | --- | --- |",
    ]
    for p in r["patamares"]:
        ok = "✅" if p["p95"] < SLA_P95_MS and p["erro"] < SLA_ERRO else "❌"
        linhas.append(f"| {p['vus']} | {p['latencia_media']:.2f} | {p['p95']:.2f} | {p['vazao']:.1f} | {p['erro']:.2f} | {ok} |")
    linhas += [
        "",
        "| Métrica (teste inteiro) | Média | Mediana | Mín | Máx | P95 | P99 |",
        "| --- | --- | --- | --- | --- | --- | --- |",
        f"| Latência, `http_req_duration` (ms) | {d['media']:.2f} | {d['mediana']:.2f} | {d['min']:.2f} | {d['max']:.2f} | {d['p95']:.2f} | {d['p99']:.2f} |",
    ]
    if b:
        linhas.append(f"| Tempo bloqueado, `http_req_blocked` (ms) | {b['media']:.2f} | {b['mediana']:.2f} | {b['min']:.2f} | {b['max']:.2f} | {b['p95']:.2f} | {b['p99']:.2f} |")
    linhas += [
        "",
        f"![Latência de {rotulo}]({pasta}/{servico}-latencia.png)",
        f"![Vazão de {rotulo}]({pasta}/{servico}-vazao.png)",
        f"![Erros de {rotulo}]({pasta}/{servico}-erros.png)",
        f"![Tempo bloqueado de {rotulo}]({pasta}/{servico}-bloqueio.png)",
    ]
    return "\n".join(linhas)


def preencher(readme, marcador, conteudo):
    padrao = re.compile(rf"(<!-- {re.escape(marcador)} -->)(.*?)(<!-- /{re.escape(marcador)} -->)", re.S)
    if not padrao.search(readme):
        print(f"  aviso: marcador {marcador} nao encontrado no README.md")
        return readme
    return padrao.sub(lambda m: f"{m.group(1)}\n{conteudo}\n{m.group(3)}", readme)


# ---------- Principal ----------

def main():
    medicao = sys.argv[1] if len(sys.argv) > 1 else "1"
    pasta = os.path.join(AQUI, "resultados", f"medicao-{medicao}")
    resumos, datas = {}, {}
    for servico, rotulo in SERVICOS:
        caminho = os.path.join(pasta, "bruto", f"{servico}.csv")
        if not os.path.exists(caminho):
            print(f"- {rotulo}: sem {caminho}, pulei")
            continue
        print(f"- {rotulo}: lendo {caminho}")
        dados, data = ler_csv(caminho)
        r = resumir(dados)
        resumos[servico], datas[servico] = r, data
        base = os.path.join(pasta, servico)
        t = f"{rotulo} (medição {medicao})"
        grafico_no_tempo(dados, "http_req_duration", "media", "Latência média (ms)", f"{t}: latência ao longo do tempo",
                         CORES[servico], f"{base}-latencia.png", percentis=True)
        grafico_no_tempo(dados, "http_reqs", "soma", "Vazão (req/s)", f"{t}: vazão ao longo do tempo",
                         CORES[servico], f"{base}-vazao.png")
        grafico_no_tempo(dados, "http_req_failed", "porcento", "Erros (% das requisições)", f"{t}: erros ao longo do tempo",
                         "#d62728", f"{base}-erros.png")
        grafico_no_tempo(dados, "http_req_blocked", "media", "Tempo bloqueado (ms)", f"{t}: tempo bloqueado ao longo do tempo",
                         "#ff7f0e", f"{base}-bloqueio.png", percentis=True)
        d = r["duracao"]
        print(f"    latência média {d['media']:.2f} ms | mediana {d['mediana']:.2f} | máx {d['max']:.2f} | "
              f"P95 {d['p95']:.2f} | P99 {d['p99']:.2f} | limite de concorrência no SLA: {r['limite']} VUs")
        for p in r["patamares"]:
            print(f"    {p['vus']:>3} VUs: {p['latencia_media']:.2f} ms, p95 {p['p95']:.2f} ms, "
                  f"{p['vazao']:.1f} req/s, {p['erro']:.2f}% de erros")

    if not resumos:
        print("Nenhum resultado encontrado. Rode o rodar-testes.bat antes.")
        return

    with open(os.path.join(pasta, "resumo.json"), "w", encoding="utf-8") as f:
        json.dump({"servicos": resumos, "datas": {s: d.strftime("%d/%m/%Y") for s, d in datas.items()}}, f, indent=2, ensure_ascii=False)
    grafico_servicos(resumos, f"Medição {medicao}: comparação entre os serviços", os.path.join(pasta, "comparacao-servicos.png"))

    with open(README, encoding="utf-8") as f:
        readme = f.read()
    for servico, rotulo in SERVICOS:
        if servico in resumos:
            readme = preencher(readme, f"MEDICAO{medicao}:{servico}",
                               texto_medicao(servico, rotulo, resumos[servico], datas[servico], medicao))
    readme = preencher(readme, f"SERVICOS:{medicao}",
                       f"![Comparação entre os serviços](resultados/medicao-{medicao}/comparacao-servicos.png)")

    # Medicao 2: graficos comparando com a medicao 1
    anterior = os.path.join(AQUI, "resultados", "medicao-1", "resumo.json")
    if medicao == "2" and os.path.exists(anterior):
        with open(anterior, encoding="utf-8") as f:
            resumos1 = json.load(f)["servicos"]
        for servico, rotulo in SERVICOS:
            if servico in resumos and servico in resumos1:
                arquivo = f"resultados/comparacao-{servico}.png"
                grafico_medicoes(servico, rotulo, resumos1[servico], resumos[servico], os.path.join(AQUI, arquivo))
                readme = preencher(readme, f"COMPARACAO:{servico}", f"![{rotulo}: medição 1 x medição 2]({arquivo})")

    with open(README, "w", encoding="utf-8") as f:
        f.write(readme)
    print(f"\nGráficos em {pasta}\nREADME.md atualizado com os números da medição {medicao}.")


if __name__ == "__main__":
    main()
