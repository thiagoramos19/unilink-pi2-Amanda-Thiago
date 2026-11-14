// Servico 2: Agenda de eventos (GET /api/eventos?email=, UC06). Operacao: leitura.
import { check, sleep } from "k6";
import { opcoes, estudante, getJson } from "./comum.js";

export const options = opcoes;

export default function () {
    const r = getJson(`/api/eventos?email=${estudante(__VU)}`, "GET /api/eventos");
    check(r, { "agenda carregada (200)": (res) => res.status === 200 });
    sleep(1);
}
