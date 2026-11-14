// Configuracao comum aos testes de carga de todos os servicos.
// Todos usam o mesmo perfil de carga, para dar para comparar um servico com o outro:
// a concorrencia sobe em patamares de 10, 50, 100, 200 e 500 usuarios virtuais (VUs),
// com 10 s de subida e 40 s parado em cada patamar.
import http from "k6/http";
import exec from "k6/execution";

export const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

// Quantidade de estudantes criados pelo massa-de-dados.sql
export const ESTUDANTES = 2000;

export const PATAMARES = [10, 50, 100, 200, 500];
const SUBIDA = 10;
const PARADO = 40;

const stages = [];
for (const vus of PATAMARES) {
    stages.push({ duration: `${SUBIDA}s`, target: vus });
    stages.push({ duration: `${PARADO}s`, target: vus });
}
stages.push({ duration: "10s", target: 0 });

export const opcoes = {
    stages,
    // SLA (RNF002 e RNF003): 95% das respostas em ate 2 s e menos de 1% de erros
    thresholds: {
        http_req_duration: ["p(95)<2000"],
        http_req_failed: ["rate<0.01"],
    },
};

// Em qual patamar o teste esta agora: "10", "50", "100", "200", ou "subida" entre um e outro.
// Vai como tag em cada requisicao, para o graficos.py separar os numeros por patamar.
export function fase() {
    const segundos = (Date.now() - exec.scenario.startTime) / 1000;
    const bloco = SUBIDA + PARADO;
    const i = Math.floor(segundos / bloco);
    if (i >= PATAMARES.length) return "descida";
    return segundos % bloco < SUBIDA ? "subida" : String(PATAMARES[i]);
}

// Cada usuario virtual usa um estudante diferente da massa de dados
export function estudante(numero) {
    return `carga${(numero % ESTUDANTES) + 1}@edu.unirio.br`;
}

export function postJson(caminho, corpo, nome) {
    return http.post(BASE_URL + caminho, JSON.stringify(corpo), {
        headers: { "Content-Type": "application/json" },
        tags: { name: nome, fase: fase() },
    });
}

export function getJson(caminho, nome) {
    return http.get(BASE_URL + caminho, { tags: { name: nome, fase: fase() } });
}
