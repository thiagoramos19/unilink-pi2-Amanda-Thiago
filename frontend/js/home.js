// Tela inicial: stories, publicar postagem, filtros e feed (UC08, UC09, UC10)

const NOMES_MIDIA = { IMAGEM: "Imagem", MUSICA: "Música", LIVRO: "Livro", ARQUIVO: "Arquivo" };
const ICONES_MIDIA = { IMAGEM: "imagem", MUSICA: "musica", LIVRO: "livro", ARQUIVO: "clipe" };

let postagens = [];
let filtroAtual = "TODOS";
let tipoMidia = "NENHUMA";

// Postagens que batem com o filtro escolhido
function postagensFiltradas() {
    if (filtroAtual === "TODOS") return postagens;
    if (filtroAtual === "EMPREGO") return postagens.filter((p) => /vaga|est[aá]gio|emprego|oportunidade/i.test(p.texto));
    return postagens.filter((p) => p.campusAutor === filtroAtual);
}

function cartaoPostagem(p) {
    const midia = NOMES_MIDIA[p.tipoMidia]
        ? `<span class="etiqueta etiqueta-midia">${icone(ICONES_MIDIA[p.tipoMidia])}${NOMES_MIDIA[p.tipoMidia]}</span>`
        : "";
    const imagem = p.imagemLocal ? `<img class="postagem-imagem" src="${p.imagemLocal}" alt="Imagem da postagem">` : "";
    const comentarios = (p.comentarios ?? []).map((c) => `
        <li>${avatar(c.nomeAutor, "avatar-mini")}<p><strong>${escapar(c.nomeAutor)}</strong> ${escapar(c.texto)}</p></li>
    `).join("");

    return `
        <article class="cartao postagem" data-id="${p.id}">
            <header class="postagem-autor">
                ${avatar(p.nomeAutor)}
                <div>
                    <h3>${escapar(p.nomeAutor)}</h3>
                    <p class="suave pequeno">${escapar(p.cursoAutor)} · ${escapar(p.campusAutor)} · ${tempoRelativo(p.dataHoraCriacao)}</p>
                </div>
            </header>
            ${p.texto ? `<p class="postagem-texto">${escapar(p.texto)}</p>` : ""}
            ${imagem}
            ${midia}
            <ul class="comentarios">${comentarios}</ul>
            <form class="comentar">
                <input class="entrada" name="comentario" placeholder="Escreva um comentário" maxlength="255" aria-label="Comentário">
                <button class="botao-icone" type="submit" aria-label="Enviar comentário">${icone("enviar")}</button>
            </form>
        </article>
    `;
}

function renderizarFeed() {
    const feed = document.getElementById("feed");
    const lista = postagensFiltradas();

    if (lista.length === 0) {
        // FE01 do UC08: nenhum post para o filtro
        feed.innerHTML = `
            <div class="aviso-vazio">
                <p>Não há publicações para esse filtro.</p>
                <button class="botao botao-secundario" type="button" id="limpar-filtro">Ver tudo</button>
            </div>`;
        document.getElementById("limpar-filtro").addEventListener("click", () => aplicarFiltro("TODOS"));
        return;
    }
    feed.innerHTML = lista.map(cartaoPostagem).join("");
}

function aplicarFiltro(filtro) {
    filtroAtual = filtro;
    document.querySelectorAll("#filtros .etiqueta").forEach((botao) => {
        botao.classList.toggle("ativo", botao.dataset.filtro === filtro);
    });
    renderizarFeed();
}

async function carregarFeed() {
    try {
        postagens = await api("/postagens");
        renderizarFeed();
    } catch {
        document.getElementById("feed").innerHTML =
            `<p class="aviso-vazio">Não foi possível carregar o feed. Confira se o back-end está ligado.</p>`;
    }
}

// ---------- Publicar postagem ----------

const formPostagem = document.getElementById("form-postagem");
const textoPostagem = document.getElementById("texto-postagem");
const previaImagem = document.getElementById("previa-imagem");

function escolherTipo(tipo) {
    tipoMidia = tipoMidia === tipo ? "NENHUMA" : tipo;
    document.querySelectorAll(".tipos-midia [data-tipo]").forEach((botao) => {
        botao.classList.toggle("ativo", botao.dataset.tipo === tipoMidia);
    });
}

document.querySelectorAll(".tipos-midia [data-tipo]").forEach((botao) => {
    botao.addEventListener("click", () => escolherTipo(botao.dataset.tipo));
});

document.getElementById("arquivo-imagem").addEventListener("change", (evento) => {
    const arquivo = evento.target.files[0];
    if (!arquivo) return;
    previaImagem.src = URL.createObjectURL(arquivo);
    previaImagem.classList.remove("oculto");
    escolherTipo("IMAGEM");
});

formPostagem.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const texto = textoPostagem.value.trim();
    const temImagem = !previaImagem.classList.contains("oculto");

    // FE01 do UC09: nao publica postagem vazia
    if (!texto && !temImagem) {
        toast("Escreva algo ou anexe uma imagem para postar.");
        textoPostagem.focus();
        return;
    }

    try {
        const nova = await enviar("POST", "/postagens", { emailAutor: EU.email, texto, tipoMidia });
        // O banco nao guarda o arquivo da imagem; a previa fica so nesta tela
        if (temImagem) nova.imagemLocal = previaImagem.src;
        postagens.unshift(nova);
    } catch (erro) {
        toast(erro.message);
        return;
    }

    formPostagem.reset();
    previaImagem.classList.add("oculto");
    escolherTipo("NENHUMA");
    aplicarFiltro("TODOS");
    toast("Postagem publicada!");
});

// ---------- Comentarios ----------

document.getElementById("feed").addEventListener("submit", async (evento) => {
    if (!evento.target.matches(".comentar")) return;
    evento.preventDefault();
    const entrada = evento.target.comentario;
    const texto = entrada.value.trim();
    if (!texto) return;

    const id = Number(evento.target.closest(".postagem").dataset.id);
    try {
        await enviar("POST", `/postagens/${id}/comentarios`, { emailAutor: EU.email, texto });
    } catch (erro) {
        toast(erro.message);
        return;
    }
    const postagem = postagens.find((p) => p.id === id);
    postagem.comentarios = [...postagem.comentarios, { nomeAutor: EU.nome, texto }];
    renderizarFeed();
});

// ---------- Stories (UC10) ----------

let stories = [];
// O banco nao guarda a foto: a que voce tirar agora fica so nesta tela
let minhaFoto = null;

// Um circulo por pessoa, com o story mais recente dela
function storiesPorAutor() {
    const vistos = new Set();
    return stories.filter((s) => !vistos.has(s.emailAutor) && vistos.add(s.emailAutor));
}

function renderizarStories() {
    document.getElementById("novo-story").classList.toggle("tem-story", stories.some((s) => s.emailAutor === EU.email));
    document.getElementById("lista-stories").innerHTML = storiesPorAutor().map((s) => `
        <button class="story" type="button" data-id="${s.id}">
            <span class="story-anel">${avatar(s.nomeAutor, "avatar-story")}</span>
            <span class="story-nome">${s.emailAutor === EU.email ? "Você" : escapar(s.nomeAutor.split(" ")[0])}</span>
        </button>
    `).join("");
}

async function carregarStories() {
    try {
        stories = await api("/stories");
    } catch {
        stories = [];
    }
    renderizarStories();
}

const janelaStory = document.getElementById("janela-story");

document.getElementById("lista-stories").addEventListener("click", (evento) => {
    const botao = evento.target.closest(".story");
    if (!botao) return;
    const s = stories.find((x) => x.id === Number(botao.dataset.id));
    document.getElementById("story-topo").innerHTML = `
        ${avatar(s.nomeAutor)}
        <div><h3>${escapar(s.nomeAutor)}</h3><p class="suave pequeno">${tempoRelativo(s.horaCriacao)}</p></div>`;
    const foto = s.emailAutor === EU.email && minhaFoto;
    document.getElementById("story-conteudo").innerHTML = foto
        ? `<img src="${foto}" alt="Story de ${escapar(s.nomeAutor)}">`
        : avatar(s.nomeAutor, "avatar-grande");
    janelaStory.showModal();
});
document.getElementById("story-fechar").addEventListener("click", () => janelaStory.close());

// Camera: abrir, capturar, publicar ou voltar
const janelaCamera = document.getElementById("janela-camera");
const video = document.getElementById("camera-video");
const fotoCapturada = document.getElementById("camera-foto");
const botaoCapturar = document.getElementById("camera-capturar");
let transmissao = null;

function desligarCamera() {
    transmissao?.getTracks().forEach((faixa) => faixa.stop());
    transmissao = null;
}

async function abrirCamera() {
    try {
        transmissao = await navigator.mediaDevices.getUserMedia({ video: { facingMode: "user" } });
    } catch {
        // FE01: permissao negada ou camera com problema
        toast("Não conseguimos abrir a câmera. Permita o acesso nas configurações do navegador e tente de novo.");
        return;
    }
    video.srcObject = transmissao;
    video.classList.remove("oculto");
    fotoCapturada.classList.add("oculto");
    botaoCapturar.textContent = "Capturar";
    janelaCamera.showModal();
}

function capturar() {
    const tela = document.createElement("canvas");
    tela.width = video.videoWidth;
    tela.height = video.videoHeight;
    const pincel = tela.getContext("2d");
    // Mesma imagem espelhada que aparece na previa
    pincel.translate(tela.width, 0);
    pincel.scale(-1, 1);
    pincel.drawImage(video, 0, 0);
    fotoCapturada.src = tela.toDataURL("image/jpeg", 0.85);
    fotoCapturada.classList.remove("oculto");
    video.classList.add("oculto");
    desligarCamera();
    botaoCapturar.textContent = "Publicar";
}

async function publicarStory() {
    try {
        await enviar("POST", "/stories", { emailAutor: EU.email });
    } catch (erro) {
        toast(erro.message);
        return;
    }
    minhaFoto = fotoCapturada.src;
    janelaCamera.close();
    toast("Story publicado! Ele some em 24 horas.");
    carregarStories();
}

document.getElementById("avatar-story").outerHTML = avatar(EU.nome, "avatar-story");
document.getElementById("novo-story").addEventListener("click", abrirCamera);
botaoCapturar.addEventListener("click", () => (transmissao ? capturar() : publicarStory()));
// FA01: voltar sem salvar
document.getElementById("camera-voltar").addEventListener("click", () => janelaCamera.close());
// Fechar a janela (inclusive pela tecla Esc) sempre desliga a camera
janelaCamera.addEventListener("close", desligarCamera);

// ---------- Proximos eventos ----------

async function carregarProximosEventos() {
    const lista = document.getElementById("proximos-eventos");
    try {
        const eventos = await api(`/eventos?email=${encodeURIComponent(EU.email)}`);
        const proximos = eventos
            .filter((e) => new Date(e.dataHora) > new Date())
            .sort((a, b) => new Date(a.dataHora) - new Date(b.dataHora))
            .slice(0, 3);
        lista.innerHTML = proximos.map((e) => {
            const data = new Date(e.dataHora);
            return `
                <li>
                    <span class="data-evento"><strong>${data.getDate()}</strong>${data.toLocaleDateString("pt-BR", { month: "short" }).replace(".", "")}</span>
                    <div><h3>${escapar(e.nome)}</h3><p class="suave pequeno">${escapar(e.local)}</p></div>
                </li>`;
        }).join("") || `<li class="suave">Nenhum evento marcado.</li>`;
    } catch {
        lista.innerHTML = `<li class="suave">Não foi possível carregar os eventos.</li>`;
    }
}

document.querySelectorAll("#filtros .etiqueta").forEach((botao) => {
    botao.addEventListener("click", () => aplicarFiltro(botao.dataset.filtro));
});

// Recado de boas-vindas logo depois do login
if (sessionStorage.getItem("unilink.boas-vindas")) {
    sessionStorage.removeItem("unilink.boas-vindas");
    toast(`Que bom te ver, ${EU.nome.split(" ")[0]}!`);
}

carregarStories();
carregarProximosEventos();
carregarFeed();
