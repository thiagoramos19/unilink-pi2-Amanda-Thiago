// Joga pra Rolo: anunciar (UC11) e comprar com Pix simulado (UC05)

const STATUS = { RESERVADO: "Reservado", VENDIDO: "Vendido" };
let produtos = [];
let abaAtual = "ofertas";
let produtoEmCompra = null;

const reais = (valor) => Number(valor).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

function itemProduto(p) {
    let acao = `<button class="botao" type="button" data-comprar="${p.id}">Comprar</button>`;
    if (p.status !== "DISPONÍVEL") acao = `<span class="etiqueta">${STATUS[p.status]}</span>`;
    if (p.emailVendedor === EU.email) acao = `<span class="etiqueta">Seu anúncio</span>`;
    if (abaAtual === "pedidos") {
        acao = `<a class="botao botao-secundario" href="mensagens.html?com=${encodeURIComponent(p.emailVendedor)}">${icone("mensagem")}Conversar</a>`;
    }

    return `
        <li class="cartao produto">
            ${avatar(p.nomeVendedor)}
            <div class="item-texto">
                <strong>${escapar(p.titulo)}</strong>
                <span class="suave pequeno">${escapar(p.nomeVendedor)}${abaAtual === "pedidos" ? ` · ${STATUS[p.status]}` : ""}</span>
            </div>
            <div class="produto-acao">
                <span class="preco">${reais(p.preco)}</span>
                ${acao}
            </div>
        </li>`;
}

async function carregarLista() {
    produtos = abaAtual === "ofertas"
        ? await api("/produtos")
        : await api(`/produtos/pedidos?email=${encodeURIComponent(EU.email)}`);
    const vazio = abaAtual === "ofertas" ? "Nenhuma oferta no momento." : "Você ainda não comprou nenhum produto.";
    document.getElementById("lista-produtos").innerHTML = produtos.length
        ? produtos.map(itemProduto).join("")
        : `<li class="aviso-vazio">${vazio}</li>`;
}

// FA01: alternar entre Ofertas e Meus pedidos
function abrirAba(aba) {
    abaAtual = aba;
    document.querySelectorAll(".abas .etiqueta").forEach((botao) => {
        botao.classList.toggle("ativo", botao.dataset.aba === aba);
        botao.setAttribute("aria-selected", botao.dataset.aba === aba);
    });
    return carregarLista();
}

document.querySelectorAll(".abas .etiqueta").forEach((botao) => {
    botao.addEventListener("click", () => abrirAba(botao.dataset.aba));
});

// ---------- Comprar ----------

const janelaPix = document.getElementById("janela-pix");

document.getElementById("lista-produtos").addEventListener("click", async (evento) => {
    const botao = evento.target.closest("[data-comprar]");
    if (!botao) return;
    try {
        const reserva = await enviar("POST", `/produtos/${botao.dataset.comprar}/reserva`, { email: EU.email });
        produtoEmCompra = reserva.produto;
        document.getElementById("pix-resumo").textContent = `${produtoEmCompra.titulo} · ${reais(produtoEmCompra.preco)}`;
        document.getElementById("pix-codigo").value = reserva.codigoPix;
        janelaPix.showModal();
    } catch (erro) {
        toast(erro.message);
    }
    carregarLista();
});

// FE01: desistir devolve o produto para Disponivel
janelaPix.addEventListener("close", async () => {
    if (!produtoEmCompra) return;
    await enviar("DELETE", `/produtos/${produtoEmCompra.id}/reserva?email=${encodeURIComponent(EU.email)}`);
    produtoEmCompra = null;
    carregarLista();
});

document.getElementById("pix-voltar").addEventListener("click", () => janelaPix.close());

document.getElementById("pix-concluir").addEventListener("click", async () => {
    const comprado = produtoEmCompra;
    produtoEmCompra = null;
    janelaPix.close();
    try {
        await enviar("POST", `/produtos/${comprado.id}/compra`, { email: EU.email });
        toast(`Seu pedido foi processado, converse com ${comprado.nomeVendedor.split(" ")[0]}!`);
        abrirAba("pedidos");
    } catch (erro) {
        toast(erro.message);
    }
});

// ---------- Anunciar ----------

const formOferta = document.getElementById("form-oferta");

formOferta.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    limparErros(formOferta);
    const titulo = document.getElementById("titulo");
    const preco = document.getElementById("preco");
    const valor = Number(preco.value.replace(",", "."));
    let ok = true;

    if (!titulo.value.trim()) {
        marcarErro(titulo, "Descreva o produto");
        ok = false;
    }
    if (!preco.value.trim() || Number.isNaN(valor) || valor < 0) {
        marcarErro(preco, "Digite um preço válido, ex.: 25,00");
        ok = false;
    }
    if (!ok) return;

    try {
        await enviar("POST", "/produtos", { emailVendedor: EU.email, titulo: titulo.value.trim(), preco: valor });
        formOferta.reset();
        toast("Oferta criada!");
        abrirAba("ofertas");
    } catch (erro) {
        toast(erro.message);
    }
});

if (EU) carregarLista();
