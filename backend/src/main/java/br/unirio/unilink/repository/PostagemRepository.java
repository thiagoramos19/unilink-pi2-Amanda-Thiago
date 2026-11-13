package br.unirio.unilink.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import br.unirio.unilink.model.Comentario;
import br.unirio.unilink.model.NovaPostagem;
import br.unirio.unilink.model.NovoComentario;
import br.unirio.unilink.model.Postagem;

/**
 * Unica classe do modulo de feed que fala com o banco. Todo o SQL fica aqui.
 */
@Repository
public class PostagemRepository {

    private final JdbcTemplate jdbc;

    public PostagemRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Postagem> listarTodas() {
        String sql = """
                SELECT p.idpostagem, p.fk_email_autor, e.nome, e.curso, e.campus_predio,
                       p.conteudo_texto, p.tipo_midia, p.data_hora_criacao
                FROM postagem p
                JOIN estudante e ON e.email_institucional = p.fk_email_autor
                ORDER BY p.data_hora_criacao DESC, p.idpostagem DESC
                """;
        return jdbc.query(sql, (rs, linha) -> new Postagem(
                rs.getInt("idpostagem"),
                rs.getString("fk_email_autor"),
                rs.getString("nome"),
                rs.getString("curso"),
                rs.getString("campus_predio"),
                rs.getString("conteudo_texto"),
                rs.getString("tipo_midia"),
                rs.getTimestamp("data_hora_criacao").toLocalDateTime(),
                List.of()));
    }

    public List<Comentario> listarComentarios(int idPostagem) {
        String sql = """
                SELECT c.idcomentario, c.fk_email_autor, e.nome, c.texto, c.data_hora
                FROM comentario c
                JOIN estudante e ON e.email_institucional = c.fk_email_autor
                WHERE c.fk_idpost = ?
                ORDER BY c.data_hora, c.idcomentario
                """;
        return jdbc.query(sql, (rs, linha) -> new Comentario(
                rs.getInt("idcomentario"),
                rs.getString("fk_email_autor"),
                rs.getString("nome"),
                rs.getString("texto"),
                rs.getTimestamp("data_hora").toLocalDateTime()), idPostagem);
    }

    public boolean existe(int idPostagem) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM postagem WHERE idpostagem = ?", Integer.class, idPostagem);
        return total > 0;
    }

    /** Grava a postagem e devolve o id gerado pelo banco. */
    public int inserir(NovaPostagem nova) {
        GeneratedKeyHolder chave = new GeneratedKeyHolder();
        jdbc.update(conexao -> {
            PreparedStatement sql = conexao.prepareStatement(
                    "INSERT INTO postagem (fk_email_autor, conteudo_texto, tipo_midia) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            sql.setString(1, nova.emailAutor());
            sql.setString(2, nova.texto());
            sql.setString(3, nova.tipoMidia());
            return sql;
        }, chave);
        return chave.getKey().intValue();
    }

    public void inserirComentario(int idPostagem, NovoComentario novo) {
        jdbc.update("INSERT INTO comentario (fk_idpost, fk_email_autor, texto) VALUES (?, ?, ?)",
                idPostagem, novo.emailAutor(), novo.texto());
    }
}
