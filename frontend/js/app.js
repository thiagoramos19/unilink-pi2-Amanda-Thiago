// ==========================================================
// UNILINK · Funcoes usadas por todas as telas
// ==========================================================

const API = "/api";

// ---------- Sessao ----------
// Depois do login, o estudante devolvido pela API fica guardado no navegador.

function usuarioAtual() {
    const salvo = localStorage.getItem("unilink.usuario");
    return salvo ? JSON.parse(salvo) : null;
}

function salvarUsuario(usuario) {
    localStorage.setItem("unilink.usuario", JSON.stringify(usuario));
}

// Estudante logado (null na tela de login e no cadastro)
const EU = usuarioAtual();

// ---------- Comunicacao com o back-end ----------

// Chama a API e devolve o JSON da resposta.
// Se a API recusar o pedido, o erro traz a mensagem dela (ex.: "Email ou senha incorretos").
async function api(caminho, opcoes = {}) {
    const resposta = await fetch(API + caminho, {
        ...opcoes,
        headers: { "Content-Type": "application/json" },
        body: opcoes.body === undefined ? undefined : JSON.stringify(opcoes.body),
    });
    const dados = resposta.status === 204 ? null : await resposta.json().catch(() => null);
    if (!resposta.ok) {
        const erro = new Error(dados?.message || `Erro ${resposta.status}`);
        erro.status = resposta.status;
        throw erro;
    }
    return dados;
}

const enviar = (metodo, caminho, corpo) => api(caminho, { method: metodo, body: corpo });

// ---------- Pedacos de HTML ----------

function icone(nome) {
    return `<svg class="icone" aria-hidden="true"><use href="img/icones.svg#${nome}"/></svg>`;
}

// Evita que um texto digitado pelo usuario vire HTML na pagina
function escapar(texto) {
    const div = document.createElement("div");
    div.textContent = texto ?? "";
    return div.innerHTML;
}

function iniciais(nome) {
    const partes = nome.trim().split(/\s+/);
    const ultima = partes.length > 1 ? partes[partes.length - 1][0] : "";
    return (partes[0][0] + ultima).toUpperCase();
}

// O banco nao guarda foto, entao cada pessoa ganha um circulo com as iniciais.
// A cor e sempre a mesma para o mesmo nome.
function avatar(nome, classeExtra = "") {
    let soma = 0;
    for (const letra of nome) soma += letra.charCodeAt(0);
    return `<span class="avatar tom-${soma % 4} ${classeExtra}" aria-hidden="true">${escapar(iniciais(nome))}</span>`;
}

function toast(mensagem) {
    let aviso = document.querySelector(".toast");
    if (!aviso) {
        aviso = document.createElement("div");
        aviso.className = "toast";
        aviso.setAttribute("role", "status");
        document.body.append(aviso);
    }
    aviso.textContent = mensagem;
    aviso.classList.add("visivel");
    clearTimeout(aviso.timer);
    aviso.timer = setTimeout(() => aviso.classList.remove("visivel"), 3200);
}

// ---------- Formularios ----------

// RNF007: borda vermelha e mensagem especifica no campo com problema
function marcarErro(entrada, mensagem) {
    const campo = entrada.closest(".campo");
    campo.classList.add("com-erro");
    campo.querySelector(".mensagem-erro").textContent = mensagem;
}

function limparErros(formulario) {
    formulario.querySelectorAll(".com-erro").forEach((campo) => {
        campo.classList.remove("com-erro");
        campo.querySelector(".mensagem-erro").textContent = "";
    });
}

// RNF005: o olho ao lado da senha mostra e esconde o que foi digitado
function ligarRevelarSenha() {
    document.querySelectorAll("[data-revelar]").forEach((botao) => {
        botao.addEventListener("click", () => {
            const entrada = document.getElementById(botao.dataset.revelar);
            const mostrar = entrada.type === "password";
            entrada.type = mostrar ? "text" : "password";
            botao.innerHTML = icone(mostrar ? "olho-fechado" : "olho");
            botao.setAttribute("aria-label", mostrar ? "Esconder senha" : "Mostrar senha");
        });
    });
}

// Contador "12/120" embaixo dos campos com limite de caracteres
function ligarContadores() {
    document.querySelectorAll("[maxlength][data-contador]").forEach((entrada) => {
        const contador = document.getElementById(entrada.dataset.contador);
        const atualizar = () => {
            contador.textContent = `${entrada.value.length}/${entrada.maxLength}`;
            contador.classList.toggle("no-limite", entrada.value.length >= entrada.maxLength);
        };
        entrada.addEventListener("input", atualizar);
        atualizar();
    });
}

montarLayout();
ligarRevelarSenha();
ligarContadores();
