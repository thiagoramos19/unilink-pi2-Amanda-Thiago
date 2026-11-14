// Servico 3: Confirmar presenca em evento (POST /api/eventos/{id}/presenca, UC06).
// Operacao: insercao (presenca_evento) e atualizacao (contador do evento).
// Cada iteracao usa um par (estudante, evento) diferente, porque o mesmo estudante
// nao pode confirmar duas vezes o mesmo evento. Para repetir este teste, recrie o banco antes.
import http from "k6/http";
import exec from "k6/execution";
import { check, sleep } from "k6";
import { opcoes, estudante, postJson, BASE_URL, ESTUDANTES } from "./comum.js";

export const options = opcoes;

// Antes do teste: pega os ids dos 50 eventos criados pela massa de dados
export function setup() {
    const eventos = http.get(`${BASE_URL}/api/eventos`).json();
    const ids = eventos.filter((e) => e.nome.startsWith("Evento de carga")).map((e) => e.id);
    if (ids.length === 0) throw new Error("Rode o massa-de-dados.sql antes deste teste");
    return { ids };
}

export default function (dados) {
    const n = exec.scenario.iterationInTest;
    const id = dados.ids[Math.floor(n / ESTUDANTES) % dados.ids.length];
    const r = postJson(`/api/eventos/${id}/presenca`, { email: estudante(n) }, "POST /api/eventos/{id}/presenca");
    check(r, { "presenca confirmada (200)": (res) => res.status === 200 });
    sleep(1);
}
