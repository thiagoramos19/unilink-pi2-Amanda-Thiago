// Agenda de eventos e confirmar presenca (UC06)

let eventos = [];
let eventoAberto = null;

const formatarData = (data) => new Date(data).toLocaleString("pt-BR", {
    weekday: "short", day: "2-digit", month: "long", hour: "2-digit", minute: "2-digit",
});
const jaPassou = (evento) => new Date(evento.dataHora) < new Date();

function eventosFiltrados() {
    const mes = document.getElementById("filtro-mes").value;
    const campus = document.getElementById("filtro-campus").value;
    return eventos
        .filter((e) => !mes || new Date(e.dataHora).getMonth() + 1 === Number(mes))
        .filter((e) => !campus || e.local.includes(campus))
        // Proximos primeiro; os que ja passaram vao para o fim da lista
        .sort((a, b) => jaPassou(a) - jaPassou(b) || new Date(a.dataHora) - new Date(b.dataHora));
}

// Os meses do filtro sao os meses que tem evento
function preencherMeses() {
    const meses = [...new Set(eventos.map((e) => new Date(e.dataHora).getMonth() + 1))].sort((a, b) => a - b);
    document.getElementById("filtro-mes").insertAdjacentHTML("beforeend", meses.map((m) => {
        const nome = new Date(2026, m - 1, 1).toLocaleDateString("pt-BR", { month: "long" });
        return `<option value="${m}">${nome[0].toUpperCase() + nome.slice(1)}</option>`;
    }).join(""));
}

function renderizarLista() {
    const lista = eventosFiltrados();
    document.getElementById("lista-eventos").innerHTML = lista.length
        ? lista.map((e) => {
            const data = new Date(e.dataHora);
            return `
                <li>
                    <button class="cartao item-lista ${e.id === eventoAberto?.id ? "ativo" : ""}" type="button" data-id="${e.id}">
                        <span class="data-evento"><strong>${data.getDate()}</strong>${data.toLocaleDateString("pt-BR", { month: "short" }).replace(".", "")}</span>
                        <span class="item-texto">
                            <strong>${escapar(e.nome)}</strong>
                            <span class="suave pequeno">${escapar(e.local)}${jaPassou(e) ? " · encerrado" : ""}</span>
                        </span>
                    </button>
                </li>`;
        }).join("")
        : `<li class="aviso-vazio">Nenhum evento com esses filtros.</li>`;
}

function renderizarDetalhe() {
    const e = eventoAberto;

    // FE01: evento que ja passou nao aceita confirmacao
    let acao = `<button class="botao botao-largo" type="button" id="confirmar">Confirmar presença</button>`;
    if (e.confirmei) acao = `<p class="confirmado">${icone("certo")}Presença confirmada! Aproveite!</p>`;
    if (jaPassou(e)) acao = `<p class="encerrado">Evento encerrado</p>`;

    document.getElementById("detalhe-evento").innerHTML = `
        <button class="botao botao-contorno so-celular" type="button" id="voltar-lista">${icone("voltar")}Eventos</button>
        <article class="cartao detalhe">
            <h2>${escapar(e.nome)}</h2>
            <ul class="detalhe-info">
                <li>${icone("relogio")}${formatarData(e.dataHora)}</li>
                <li>${icone("local")}${escapar(e.local)}</li>
                <li>${icone("perfil")}Organizado por ${escapar(e.organizador)}</li>
            </ul>
            <p class="contagem"><strong>${e.confirmados}</strong> confirmaram presença</p>
            ${acao}
            <button class="botao botao-secundario botao-largo" type="button" id="ver-mapa">${icone("local")}Como chegar?</button>
        </article>`;

    document.getElementById("confirmar")?.addEventListener("click", confirmarPresenca);
    document.getElementById("ver-mapa").addEventListener("click", () => {
        document.getElementById("mapa-local").textContent = e.local;
        document.getElementById("janela-mapa").showModal();
    });
    document.getElementById("voltar-lista").addEventListener("click", () => {
        document.getElementById("eventos").classList.remove("mostrando-detalhe");
    });
}

async function confirmarPresenca() {
    try {
        const atualizado = await enviar("POST", `/eventos/${eventoAberto.id}/presenca`, { email: EU.email });
        eventos[eventos.indexOf(eventoAberto)] = atualizado;
        eventoAberto = atualizado;
        renderizarDetalhe();
    } catch (erro) {
        toast(erro.message);
    }
}

document.getElementById("lista-eventos").addEventListener("click", (evento) => {
    const botao = evento.target.closest("[data-id]");
    if (!botao) return;
    eventoAberto = eventos.find((e) => e.id === Number(botao.dataset.id));
    document.getElementById("eventos").classList.add("mostrando-detalhe");
    renderizarLista();
    renderizarDetalhe();
});

document.getElementById("filtro-mes").addEventListener("change", renderizarLista);
document.getElementById("filtro-campus").addEventListener("change", renderizarLista);

async function carregarEventos() {
    eventos = await api(`/eventos?email=${encodeURIComponent(EU.email)}`);
    preencherMeses();
    renderizarLista();
}

if (EU) carregarEventos();
