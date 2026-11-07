// Seu perfil e editar biografia (UC07)

const CAMPOS_BIO = [
    { chave: "gostos", rotulo: "Três coisas que amo" },
    { chave: "frase", rotulo: "Uma frase que me define" },
    { chave: "mood", rotulo: "Mood atual" },
    { chave: "funfact", rotulo: "Fun fact / red flag" },
];

const formBio = document.getElementById("form-bio");
const leitura = document.getElementById("bio-leitura");
const botaoEditar = document.getElementById("editar-bio");
let biografia = {};

function renderizarBiografia() {
    leitura.innerHTML = CAMPOS_BIO.map(({ chave, rotulo }) => `
        <dt>${rotulo}</dt>
        <dd>${biografia[chave] ? escapar(biografia[chave]) : '<span class="suave">Ainda não preenchido</span>'}</dd>
    `).join("");
}

async function carregarPerfil() {
    const perfil = await api(`/estudantes/${encodeURIComponent(EU.email)}/perfil`);
    const { estudante } = perfil;
    document.getElementById("perfil-avatar").innerHTML = avatar(estudante.nome, "avatar-grande");
    document.getElementById("perfil-nome").textContent = estudante.nome;
    document.getElementById("perfil-curso").textContent = `${estudante.curso} · ${estudante.campus}`;
    document.getElementById("qtd-amigos").textContent = perfil.amigos;
    document.getElementById("qtd-eventos").textContent = perfil.eventos;
    biografia = perfil.biografia;
    renderizarBiografia();
}

function modoEdicao(editando) {
    formBio.classList.toggle("oculto", !editando);
    leitura.classList.toggle("oculto", editando);
    botaoEditar.classList.toggle("oculto", editando);
}

botaoEditar.addEventListener("click", () => {
    CAMPOS_BIO.forEach(({ chave }) => {
        const entrada = document.getElementById(`bio-${chave}`);
        entrada.value = biografia[chave] ?? "";
        entrada.dispatchEvent(new Event("input"));
    });
    modoEdicao(true);
});

// FA01: voltar descarta o que foi digitado
document.getElementById("cancelar-bio").addEventListener("click", () => modoEdicao(false));

formBio.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const nova = {};
    CAMPOS_BIO.forEach(({ chave }) => {
        nova[chave] = document.getElementById(`bio-${chave}`).value.trim();
    });
    try {
        biografia = await enviar("PUT", `/estudantes/${encodeURIComponent(EU.email)}/biografia`, nova);
        renderizarBiografia();
        modoEdicao(false);
        toast("Perfil atualizado com sucesso!");
    } catch (erro) {
        toast(erro.message);
    }
});

document.getElementById("sair").addEventListener("click", sair);

if (EU) carregarPerfil();
