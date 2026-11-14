// Servico 4 (incluido na medicao 2): Feed (GET /api/postagens, UC08). Operacao: leitura.
// Devolve todas as postagens, cada uma com os seus comentarios.
import { check, sleep } from "k6";
import { opcoes, getJson } from "./comum.js";

export const options = opcoes;

export default function () {
    const r = getJson("/api/postagens", "GET /api/postagens");
    check(r, { "feed carregado (200)": (res) => res.status === 200 });
    sleep(1);
}
