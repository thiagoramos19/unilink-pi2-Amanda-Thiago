// Cards de descobrir e adicionar amigo (UC03)

let mao = [];
let selecionada = null;
let virada = false;

// Pede ao back-end ate 3 estudantes que ainda nao sao seus amigos
async function embaralhar() {
    try {
        mao = await api(`/cartas?email=${encodeURIComponent(EU.email)}`);
    } catch (erro) {
        toast(erro.message);
        mao = [];
    }
    selecionada = mao[0] ?? null;
    virada = false;
    renderizar();
}

function frenteCarta(p) {
    return `
        <div class="carta-face carta-frente">
            ${avatar(p.nome, "avatar-grande")}
            <h2>${escapar(p.nome)}, ${p.idade}</h2>
            <p class="suave">${escapar(p.curso)}</p>
            <span class="etiqueta">${icone("local")}${escapar(p.campus)}</span>
        </div>`;
}

function versoCarta(p) {
    const bio = p.biografia ?? {};
    const gostos = (bio.gostos ?? "").split(",").filter((g) => g.trim())
        .map((g) => `<li>${escapar(g.trim())}</li>`).join("");
    const trecho = (titulo, texto) => (texto ? `<h3>${titulo}</h3><p>${escapar(texto)}</p>` : "");
    const vazia = !gostos && !bio.frase && !bio.mood && !bio.funfact;
    return `
        <div class="carta-face carta-verso">
            ${gostos ? `<h3>Três coisas que amo</h3><ul class="lista-pontos">${gostos}</ul>` : ""}
            ${trecho("Sobre mim", bio.frase)}
            ${trecho("Mood atual", bio.mood)}
            ${trecho("Fun fact / red flag", bio.funfact)}
            ${vazia ? `<p class="suave">Essa pessoa ainda não escreveu a biografia.</p>` : ""}
        </div>`;
}

function renderizar() {
    const palco = document.getElementById("palco");
    const areaMao = document.getElementById("mao");

    // FE01: acabaram os perfis
    if (!selecionada) {
        palco.innerHTML = `<div class="aviso-vazio cartao"><p>Não há mais cartas disponíveis por enquanto.</p></div>`;
        areaMao.innerHTML = "";
        return;
    }

    palco.innerHTML = `
        <div class="carta ${virada ? "virada" : ""}">
            ${frenteCarta(selecionada)}
            ${versoCarta(selecionada)}
        </div>
        <div class="palco-acoes">
            <button class="botao botao-contorno" type="button" id="virar">${virada ? "Ver a frente" : "Virar a carta"}</button>
            <button class="botao" type="button" id="adicionar">${icone("adicionar-amigo")}Adicionar amigo</button>
        </div>`;

    areaMao.innerHTML = mao.map((p, i) => `
        <button class="mini-carta ${p === selecionada ? "ativo" : ""}" type="button" role="option"
                aria-selected="${p === selecionada}" data-indice="${i}">
            ${avatar(p.nome)}
            <span>${escapar(p.nome.split(" ")[0])}</span>
        </button>`).join("");

    document.getElementById("virar").addEventListener("click", () => {
        virada = !virada;
        renderizar();
    });
    document.getElementById("adicionar").addEventListener("click", adicionarAmigo);
}

async function adicionarAmigo() {
    try {
        await enviar("POST", "/amizades", { solicitante: EU.email, solicitado: selecionada.email });
    } catch (erro) {
        toast(erro.message);
        return;
    }
    toast(`Você adicionou ${selecionada.nome.split(" ")[0]} aos amigos, entre no Chat para conversar com ele(a)!`);
    mao.splice(mao.indexOf(selecionada), 1);
    selecionada = mao[0] ?? null;
    virada = false;
    // Acabou a mao: busca novas cartas (ou mostra o aviso do FE01)
    if (!selecionada) embaralhar();
    else renderizar();
}

document.getElementById("mao").addEventListener("click", (evento) => {
    const botao = evento.target.closest(".mini-carta");
    if (!botao) return;
    const escolhida = mao[Number(botao.dataset.indice)];
    // FA02: clicar de novo na carta aberta "abaixa" ela
    virada = escolhida === selecionada ? !virada : false;
    selecionada = escolhida;
    renderizar();
});

document.getElementById("embaralhar").addEventListener("click", embaralhar);

embaralhar();
