// Mensagens privadas e Unibot (UC04)

const UNIBOT = "Unibot";
const RESPOSTAS_UNIBOT = {
    1: "Cardápio do bandejão hoje: arroz, feijão, frango assado, salada de alface e tomate. Opção vegetariana: escondidinho de legumes.",
    2: "Horário de aulas (BSI, 4º período): seg e qua 18h-22h · ter 18h-20h · qui 20h-22h.",
};
const MENU_UNIBOT = "Oi! Eu sou o Unibot. Digite 1 para ver o cardápio do bandejão ou 2 para ver o horário de aulas.";

// De quanto em quanto tempo a tela pergunta ao back-end se chegou mensagem nova
const INTERVALO_ATUALIZAR = 4000;

// O Unibot nao fica no banco: a conversa dele existe so nesta tela
const unibot = {
    email: null,
    nome: UNIBOT,
    mensagens: [{ minha: false, texto: MENU_UNIBOT, dataHora: null }],
};

let conversas = [];
let conversaAberta = null;
let mensagensAbertas = [];

const hora = (data) => (data ? new Date(data).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" }) : "");

function avatarConversa(c) {
    return c === unibot ? `<span class="avatar tom-2" aria-hidden="true">${icone("robo")}</span>` : avatar(c.nome);
}

// Ultima mensagem que aparece embaixo do nome na lista
function resumo(c) {
    if (c === unibot) {
        const ultima = unibot.mensagens[unibot.mensagens.length - 1];
        return { texto: ultima.texto, minha: ultima.minha, dataHora: ultima.dataHora };
    }
    return { texto: c.ultimaMensagem ?? "Diga oi!", minha: c.ultimaFoiMinha, dataHora: c.dataHora };
}

function renderizarLista() {
    const todas = [...conversas, unibot];
    document.getElementById("lista-conversas").innerHTML = todas.map((c, i) => {
        const r = resumo(c);
        const ativa = conversaAberta && c.email === conversaAberta.email;
        return `
            <li>
                <button class="cartao item-lista ${ativa ? "ativo" : ""}" type="button" data-indice="${i}">
                    ${avatarConversa(c)}
                    <span class="item-texto">
                        <strong>${escapar(c.nome)}</strong>
                        <span class="suave pequeno uma-linha">${r.minha ? "Você: " : ""}${escapar(r.texto)}</span>
                    </span>
                    <span class="suave pequeno">${hora(r.dataHora)}</span>
                </button>
            </li>`;
    }).join("");
}

// So os baloes; o campo de digitar nao e refeito para nao perder o texto
function renderizarBaloes() {
    const lista = document.getElementById("chat-mensagens");
    if (!lista) return;
    lista.innerHTML = mensagensAbertas.map((m) => `
        <li class="balao ${m.minha ? "meu" : ""}">${escapar(m.texto)}<span>${hora(m.dataHora)}</span></li>
    `).join("");
    lista.scrollTop = lista.scrollHeight;
}

function renderizarChat() {
    const c = conversaAberta;
    document.getElementById("chat").innerHTML = `
        <div class="cartao chat">
            <header class="chat-topo">
                <button class="botao-icone so-celular" type="button" id="voltar-lista" aria-label="Voltar para conversas">${icone("voltar")}</button>
                ${avatarConversa(c)}
                <h2>${escapar(c.nome)}</h2>
            </header>
            <ol class="chat-mensagens" id="chat-mensagens"></ol>
            <form class="chat-enviar" id="form-mensagem">
                <input class="entrada" id="texto-mensagem" placeholder="Digite uma mensagem" maxlength="1000" autocomplete="off" aria-label="Mensagem">
                <button class="botao-icone botao-enviar" type="submit" aria-label="Enviar">${icone("enviar")}</button>
            </form>
        </div>`;
    renderizarBaloes();

    document.getElementById("form-mensagem").addEventListener("submit", enviarMensagem);
    document.getElementById("voltar-lista").addEventListener("click", () => {
        document.getElementById("mensagens").classList.remove("mostrando-detalhe");
    });
    document.getElementById("texto-mensagem").focus();
}

// ---------- Conversa com o back-end ----------

async function carregarConversas() {
    try {
        conversas = await api(`/conversas?email=${encodeURIComponent(EU.email)}`);
    } catch {
        conversas = [];
    }
    renderizarLista();
}

async function carregarHistorico() {
    if (!conversaAberta || conversaAberta === unibot) return;
    const com = conversaAberta.email;
    const historico = await api(`/mensagens?email=${encodeURIComponent(EU.email)}&com=${encodeURIComponent(com)}`);
    // O estudante pode ter trocado de conversa enquanto a resposta chegava
    if (conversaAberta?.email !== com) return;
    const mudou = historico.length !== mensagensAbertas.length;
    mensagensAbertas = historico.map((m) => ({ minha: m.remetente === EU.email, texto: m.texto, dataHora: m.dataHora }));
    if (mudou) renderizarBaloes();
}

async function abrirConversa(c) {
    conversaAberta = c;
    mensagensAbertas = c === unibot ? unibot.mensagens : [];
    document.getElementById("mensagens").classList.add("mostrando-detalhe");
    renderizarLista();
    renderizarChat();
    try {
        await carregarHistorico();
    } catch (erro) {
        toast(erro.message);
    }
}

async function enviarMensagem(evento) {
    evento.preventDefault();
    const entrada = document.getElementById("texto-mensagem");
    const texto = entrada.value.trim();
    // FE01: mensagem em branco e ignorada
    if (!texto) return;
    entrada.value = "";

    if (conversaAberta === unibot) {
        const agora = new Date();
        unibot.mensagens.push({ minha: true, texto, dataHora: agora });
        unibot.mensagens.push({ minha: false, texto: RESPOSTAS_UNIBOT[texto] ?? MENU_UNIBOT, dataHora: agora });
        renderizarBaloes();
        renderizarLista();
        return;
    }

    try {
        await enviar("POST", "/mensagens", { remetente: EU.email, destinatario: conversaAberta.email, texto });
    } catch (erro) {
        entrada.value = texto;
        toast(erro.message);
        return;
    }
    await carregarHistorico();
    carregarConversas();
}

document.getElementById("lista-conversas").addEventListener("click", (evento) => {
    const botao = evento.target.closest("[data-indice]");
    if (!botao) return;
    abrirConversa([...conversas, unibot][Number(botao.dataset.indice)]);
});

async function iniciar() {
    await carregarConversas();
    // mensagens.html?com=email abre direto a conversa com esse amigo (vem do Joga pra Rolo)
    const com = new URLSearchParams(location.search).get("com");
    const amigo = conversas.find((c) => c.email === com);
    if (amigo) abrirConversa(amigo);
    else if (com) toast("Adicione essa pessoa como amiga no Descobrir para conversar.");

    // Mensagens novas aparecem sem recarregar a pagina
    setInterval(() => {
        carregarHistorico().catch(() => {});
        carregarConversas();
    }, INTERVALO_ATUALIZAR);
}

iniciar();
