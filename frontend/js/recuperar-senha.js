// Recuperar senha (UC02, FA02)

const formRecuperar = document.getElementById("form-recuperar");

formRecuperar.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    limparErros(formRecuperar);
    const email = document.getElementById("email");

    if (!email.value.trim().endsWith("@edu.unirio.br")) {
        marcarErro(email, "Email fora do formato @edu.unirio.br");
        return;
    }
    try {
        const resposta = await enviar("POST", "/recuperar-senha", { email: email.value.trim() });
        toast(resposta.mensagem);
        formRecuperar.reset();
    } catch (erro) {
        marcarErro(email, erro.message);
    }
});
