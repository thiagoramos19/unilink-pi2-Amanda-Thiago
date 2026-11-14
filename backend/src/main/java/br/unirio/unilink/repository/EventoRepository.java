package br.unirio.unilink.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import br.unirio.unilink.model.Evento;

/**
 * SQL das tabelas evento e presenca_evento.
 * O numero de confirmados e contado direto em presenca_evento (medicao 2):
 * assim confirmar presenca nao precisa escrever na linha do evento, que era
 * disputada por todo mundo que confirmava no mesmo evento.
 */
@Repository
public class EventoRepository {

    private static final String SELECT = """
            SELECT ev.idevento, ev.nome_evento, ev.data_hora_evento, ev.local_campus,
                   (SELECT COUNT(*) FROM presenca_evento c WHERE c.fk_id_evento = ev.idevento) AS qtd_confirmados,
                   org.nome AS organizador,
                   (pe.fk_id_evento IS NOT NULL) AS confirmei
            FROM evento ev
            JOIN estudante org ON org.email_institucional = ev.fk_email_organizador
            LEFT JOIN presenca_evento pe
                   ON pe.fk_id_evento = ev.idevento AND pe.fk_email_estudante = ?
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

    /** A chave primaria impede presenca repetida e a chave estrangeira impede estudante inexistente. */
    public void registrarPresenca(int id, String email) {
        jdbc.update("INSERT INTO presenca_evento (fk_email_estudante, fk_id_evento) VALUES (?, ?)", email, id);
    }
}
