package br.unirio.unilink.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import br.unirio.unilink.model.Evento;

/**
 * SQL das tabelas evento e presenca_evento.
 */
@Repository
public class EventoRepository {

    private static final String SELECT = """
            SELECT ev.idevento, ev.nome_evento, ev.data_hora_evento, ev.local_campus, ev.qtd_confirmados,
                   org.nome AS organizador,
                   EXISTS (SELECT 1 FROM presenca_evento pe
                           WHERE pe.fk_id_evento = ev.idevento AND pe.fk_email_estudante = ?) AS confirmei
            FROM evento ev
            JOIN estudante org ON org.email_institucional = ev.fk_email_organizador
            """;

    private static final RowMapper<Evento> EVENTO = (rs, linha) -> new Evento(
            rs.getInt("idevento"),
            rs.getString("nome_evento"),
            rs.getTimestamp("data_hora_evento").toLocalDateTime(),
            rs.getString("local_campus"),
            rs.getInt("qtd_confirmados"),
            rs.getString("organizador"),
            rs.getBoolean("confirmei"));

    private final JdbcTemplate jdbc;

    public EventoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Evento> listar(String email) {
        return jdbc.query(SELECT + " ORDER BY ev.data_hora_evento", EVENTO, email);
    }

    public Optional<Evento> buscar(int id, String email) {
        return jdbc.query(SELECT + " WHERE ev.idevento = ?", EVENTO, email, id).stream().findFirst();
    }

    public void confirmarPresenca(int id, String email) {
        jdbc.update("INSERT INTO presenca_evento (fk_email_estudante, fk_id_evento) VALUES (?, ?)", email, id);
        jdbc.update("UPDATE evento SET qtd_confirmados = qtd_confirmados + 1 WHERE idevento = ?", id);
    }
}
