package br.unirio.unilink.service;

import static br.unirio.unilink.service.Validacao.exigir;
import static br.unirio.unilink.service.Validacao.vazio;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import br.unirio.unilink.model.NovoProduto;
import br.unirio.unilink.model.Produto;
import br.unirio.unilink.model.Reserva;
import br.unirio.unilink.repository.EstudanteRepository;
import br.unirio.unilink.repository.ProdutoRepository;

/**
 * Regras do Joga pra Rolo: anunciar (UC11) e comprar (UC05).
 */
@Service
public class ProdutoService {

    private final ProdutoRepository repository;
    private final EstudanteRepository estudantes;

    public ProdutoService(ProdutoRepository repository, EstudanteRepository estudantes) {
        this.repository = repository;
        this.estudantes = estudantes;
    }

    public List<Produto> listar() {
        return repository.listar();
    }

    public List<Produto> pedidos(String email) {
        return repository.listarPedidos(email);
    }

    public void anunciar(NovoProduto novo) {
        // FE01 do UC11: descricao e preco obrigatorios
        exigir(!vazio(novo.titulo()), "Descreva o produto");
        exigir(novo.titulo().length() <= 100, "A descrição pode ter até 100 caracteres");
        exigir(novo.preco() != null && novo.preco().compareTo(BigDecimal.ZERO) >= 0, "Digite um preço válido");
        exigir(novo.emailVendedor() != null && estudantes.existe(novo.emailVendedor()), "Vendedor não encontrado");
        repository.inserir(new NovoProduto(novo.emailVendedor(), novo.titulo().trim(), novo.preco()));
    }

    public Reserva reservar(int id, String email) {
        Produto produto = buscar(id);
        exigir(!produto.emailVendedor().equals(email), "Você não pode comprar o seu próprio anúncio");
        if (!repository.reservar(id, email)) {
            throw Validacao.conflito("Este produto não está mais disponível");
        }
        return new Reserva(buscar(id), codigoPix(produto));
    }

    public Produto cancelarReserva(int id, String email) {
        exigir(repository.cancelarReserva(id, email), "Não há reserva sua para este produto");
        return buscar(id);
    }

    public Produto concluirCompra(int id, String email) {
        exigir(repository.concluirCompra(id, email), "Não há reserva sua para este produto");
        return buscar(id);
    }

    private Produto buscar(int id) {
        return repository.buscar(id).orElseThrow(() -> Validacao.naoEncontrado("Produto não encontrado"));
    }

    // Codigo Pix de mentira, so para simular o pagamento (o Pix real esta fora do escopo)
    private static String codigoPix(Produto produto) {
        return "00020126UNILINK" + produto.id() + "5204000053039865406" + produto.preco() + "5802BR6304SIMU";
    }
}
