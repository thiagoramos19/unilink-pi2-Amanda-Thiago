// Cadastro em 3 passos (UC01): acesso, dados pessoais e biografia

const formCadastro = document.getElementById("form-cadastro");
const botaoVoltar = document.getElementById("voltar-passo");
const botaoAvancar = document.getElementById("avancar");
let passo = 1;

const valor = (id) => document.getElementById(id).value.trim();

// Cada passo confere os proprios campos e devolve true se estiver tudo certo
const validacoes = {
    1() {
        let ok = true;
        if (!valor("email").endsWith("@edu.unirio.br")) {
            marcarErro(document.getElementById("email"), "Email fora do formato @edu.unirio.br");
            ok = false;
        }
        if (valor("senha").length < 6) {
            marcarErro(document.getElementById("senha"), "A senha precisa de pelo menos 6 caracteres");
            ok = false;
        }
        if (valor("confirmar-senha") !== valor("senha")) {
            marcarErro(document.getElementById("confirmar-senha"), "As senhas não são iguais");
            ok = false;
        }
        return ok;
    },
    2() {
        let ok = true;
        if (!valor("nome")) {
            marcarErro(document.getElementById("nome"), "Digite seu nome!");
            ok = false;
        }
        if (!valor("nascimento")) {
            marcarErro(document.getElementById("nascimento"), "Digite uma data de nascimento!");
            ok = false;
        }
        if (!valor("curso")) {
            marcarErro(document.getElementById("curso"), "Digite seu curso!");
            ok = false;
        }
        return ok;
    },
    3() {
        return true;
    },
};

function mostrarPasso(numero) {
    passo = numero;
    document.querySelectorAll(".passo").forEach((bloco) => {
        bloco.classList.toggle("oculto", Number(bloco.dataset.passo) !== passo);
    });
    document.getElementById("passo-atual").textContent = passo;
    document.getElementById("barra-progresso").style.width = `${(passo / 3) * 100}%`;
    botaoVoltar.classList.toggle("oculto", passo === 1);
    botaoAvancar.textContent = passo === 3 ? "Criar conta" : "Prosseguir";
    document.querySelector(`[data-passo="${passo}"] .entrada`).focus();
}

botaoVoltar.addEventListener("click", () => {
    limparErros(formCadastro);
    mostrarPasso(passo - 1);
});

formCadastro.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    limparErros(formCadastro);
    if (!validacoes[passo]()) return;

    if (passo < 3) {
        mostrarPasso(passo + 1);
        return;
    }

    try {
        const estudante = await enviar("POST", "/estudantes", {
            email: valor("email"),
            senha: valor("senha"),
            nome: valor("nome"),
            dataNascimento: valor("nascimento"),
            curso: valor("curso"),
            campus: valor("campus"),
            gostos: valor("gostos"),
            frase: valor("frase"),
            mood: valor("mood"),
            funfact: valor("funfact"),
        });
        salvarUsuario(estudante);
        location.href = "home.html";
    } catch (erro) {
        // E-mail ja cadastrado: volta para o primeiro passo e mostra o erro no campo
        if (erro.status === 409) {
            mostrarPasso(1);
            marcarErro(document.getElementById("email"), erro.message);
        } else {
            toast(erro.message);
        }
    }
});

mostrarPasso(1);
