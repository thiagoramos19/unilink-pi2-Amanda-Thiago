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

    /**
     * Soma 1 no contador, mas so se o evento existir e ainda nao tiver acontecido.
     * Por ser o primeiro comando da transacao, ja trava a linha do evento para escrita:
     * quem chegar depois espera na fila, em vez de entrar em deadlock (medicao 1).
     * Devolve false quando nenhuma linha foi alterada.
     */
    public boolean somarConfirmado(int id) {
        return jdbc.update("""
                UPDATE evento SET qtd_confirmados = qtd_confirmados + 1
                WHERE idevento = ? AND data_hora_evento > NOW()
                """, id) > 0;
    }

    public boolean existe(int id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM evento WHERE idevento = ?", Integer.class, id) > 0;
    }

    /** A chave primaria impede presenca repetida e a chave estrangeira impede estudante inexistente. */
    public void registrarPresenca(int id, String email) {
        jdbc.update("INSERT INTO presenca_evento (fk_email_estudante, fk_id_evento) VALUES (?, ?)", email, id);
    }
}
