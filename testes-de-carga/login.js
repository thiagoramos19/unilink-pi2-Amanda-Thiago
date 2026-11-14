// Servico 1: Login (POST /api/login, UC02). Operacao: leitura.
import { check, sleep } from "k6";
import { opcoes, estudante, postJson } from "./comum.js";

export const options = opcoes;

export default function () {
    const r = postJson("/api/login", { email: estudante(__VU), senha: "123456" }, "POST /api/login");
    check(r, { "login aceito (200)": (res) => res.status === 200 });
    sleep(1);
}
