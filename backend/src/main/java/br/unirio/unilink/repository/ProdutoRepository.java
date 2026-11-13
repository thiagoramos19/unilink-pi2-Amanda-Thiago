package br.unirio.unilink.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import br.unirio.unilink.model.NovoProduto;
import br.unirio.unilink.model.Produto;

/**
 * SQL da tabela produto. As mudancas de status so acontecem se o produto
 * estiver no status esperado, para dois compradores nao reservarem o mesmo item.
 */
@Repository
public class ProdutoRepository {

    private static final String SELECT = """
            SELECT p.idproduto, p.titulo, p.preco, p.status, p.fk_email_vendedor, e.nome, p.fk_email_reserva
            FROM produto p
            JOIN estudante e ON e.email_institucional = p.fk_email_vendedor
            """;

    private static final RowMapper<Produto> PRODUTO = (rs, linha) -> new Produto(
            rs.getInt("idproduto"),
            rs.getString("titulo"),
            rs.getBigDecimal("preco"),
            rs.getString("status"),
            rs.getString("fk_email_vendedor"),
            rs.getString("nome"),
            rs.getString("fk_email_reserva"));

    private final JdbcTemplate jdbc;

    public ProdutoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Produto> listar() {
        return jdbc.query(SELECT + " ORDER BY p.idproduto DESC", PRODUTO);
    }

    public List<Produto> listarPedidos(String email) {
        return jdbc.query(SELECT + " WHERE p.fk_email_reserva = ? ORDER BY p.idproduto DESC", PRODUTO, email);
    }

    public Optional<Produto> buscar(int id) {
        return jdbc.query(SELECT + " WHERE p.idproduto = ?", PRODUTO, id).stream().findFirst();
    }

    public void inserir(NovoProduto novo) {
        jdbc.update("INSERT INTO produto (fk_email_vendedor, titulo, preco) VALUES (?, ?, ?)",
                novo.emailVendedor(), novo.titulo(), novo.preco());
    }

    /** DISPONÍVEL -> RESERVADO. Devolve false se outra pessoa reservou antes. */
    public boolean reservar(int id, String email) {
        return jdbc.update("""
                UPDATE produto SET status = 'RESERVADO', fk_email_reserva = ?
                WHERE idproduto = ? AND status = 'DISPONÍVEL' AND fk_email_vendedor <> ?
                """, email, id, email) == 1;
    }

    /** RESERVADO -> DISPONÍVEL (comprador desistiu). */
    public boolean cancelarReserva(int id, String email) {
        return jdbc.update("""
                UPDATE produto SET status = 'DISPONÍVEL', fk_email_reserva = NULL
                WHERE idproduto = ? AND status = 'RESERVADO' AND fk_email_reserva = ?
                """, id, email) == 1;
    }

    /** RESERVADO -> VENDIDO (Pix concluido). */
    public boolean concluirCompra(int id, String email) {
        return jdbc.update("""
                UPDATE produto SET status = 'VENDIDO'
                WHERE idproduto = ? AND status = 'RESERVADO' AND fk_email_reserva = ?
                """, id, email) == 1;
    }
}
