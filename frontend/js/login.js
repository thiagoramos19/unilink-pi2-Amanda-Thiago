// Tela de login (UC02)

const formLogin = document.getElementById("form-login");

formLogin.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    limparErros(formLogin);

    const email = document.getElementById("email");
    const senha = document.getElementById("senha");
    let valido = true;

    if (!email.value.trim().endsWith("@edu.unirio.br")) {
        marcarErro(email, "Email fora do formato @edu.unirio.br");
        valido = false;
    }
    if (!senha.value) {
        marcarErro(senha, "Digite sua senha");
        valido = false;
    }
    if (!valido) return;

    try {
        const estudante = await enviar("POST", "/login", { email: email.value.trim(), senha: senha.value });
        salvarUsuario(estudante);
        sessionStorage.setItem("unilink.boas-vindas", "1");
        location.href = "home.html";
    } catch (erro) {
        // FE01: credenciais incorretas deixam os dois campos em vermelho
        marcarErro(email, "");
        marcarErro(senha, erro.message);
    }
});
