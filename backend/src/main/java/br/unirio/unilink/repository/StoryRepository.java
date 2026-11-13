package br.unirio.unilink.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.unirio.unilink.model.Story;

/**
 * SQL da tabela story.
 */
@Repository
public class StoryRepository {

    private final JdbcTemplate jdbc;

    public StoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Stories dentro das 24 horas, do mais novo para o mais antigo. */
    public List<Story> listarAtivos() {
        String sql = """
                SELECT s.idstory, s.fk_email_autor, e.nome, s.hora_criacao, s.hora_fim
                FROM story s
                JOIN estudante e ON e.email_institucional = s.fk_email_autor
                WHERE s.hora_fim > NOW()
                ORDER BY s.hora_criacao DESC, s.idstory DESC
                """;
        return jdbc.query(sql, (rs, linha) -> new Story(
                rs.getInt("idstory"),
                rs.getString("fk_email_autor"),
                rs.getString("nome"),
                rs.getTimestamp("hora_criacao").toLocalDateTime(),
                rs.getTimestamp("hora_fim").toLocalDateTime()));
    }

    /** RN: o story fica no ar por 24 horas a partir de agora. */
    public void inserir(String emailAutor) {
        jdbc.update("INSERT INTO story (fk_email_autor, hora_criacao, hora_fim) VALUES (?, NOW(), NOW() + INTERVAL 24 HOUR)",
                emailAutor);
    }
}
