package br.unirio.unilink.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.unirio.unilink.model.Conversa;
import br.unirio.unilink.model.Mensagem;
import br.unirio.unilink.model.NovaMensagem;

/**
 * SQL da tabela mensagem e da lista de conversas.
 */
@Repository
public class MensagemRepository {

    private final JdbcTemplate jdbc;

    public MensagemRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Amigos de quem pediu, cada um com a ultima mensagem trocada. */
    public List<Conversa> listarConversas(String email) {
        String sql = """
                SELECT e.email_institucional, e.nome, m.texto_mensagem, m.fk_email_remetente, m.data_hora_envio
                FROM solicitacao_amizade s
                JOIN estudante e ON e.email_institucional =
                     IF(s.fk_email_solicitante = ?, s.fk_email_solicitado, s.fk_email_solicitante)
                LEFT JOIN mensagem m ON m.idmensagem = (
                     SELECT MAX(m2.idmensagem) FROM mensagem m2
                     WHERE (m2.fk_email_remetente = ? AND m2.fk_email_destinatario = e.email_institucional)
                        OR (m2.fk_email_destinatario = ? AND m2.fk_email_remetente = e.email_institucional))
                WHERE s.status = 'ACEITO' AND ? IN (s.fk_email_solicitante, s.fk_email_solicitado)
                ORDER BY m.data_hora_envio IS NULL, m.data_hora_envio DESC, e.nome
                """;
        return jdbc.query(sql, (rs, linha) -> {
            Timestamp dataHora = rs.getTimestamp("data_hora_envio");
            return new Conversa(
                    rs.getString("email_institucional"),
                    rs.getString("nome"),
                    rs.getString("texto_mensagem"),
                    email.equals(rs.getString("fk_email_remetente")),
                    dataHora == null ? null : dataHora.toLocalDateTime());
        }, email, email, email, email);
    }

    /** Historico entre duas pessoas, da mais antiga para a mais nova. */
    public List<Mensagem> listarHistorico(String um, String outro) {
        String sql = """
                SELECT idmensagem, fk_email_remetente, fk_email_destinatario, texto_mensagem, data_hora_envio
                FROM mensagem
                WHERE (fk_email_remetente = ? AND fk_email_destinatario = ?)
                   OR (fk_email_remetente = ? AND fk_email_destinatario = ?)
                ORDER BY data_hora_envio, idmensagem
                """;
        return jdbc.query(sql, (rs, linha) -> new Mensagem(
                rs.getInt("idmensagem"),
                rs.getString("fk_email_remetente"),
                rs.getString("fk_email_destinatario"),
                rs.getString("texto_mensagem"),
                rs.getTimestamp("data_hora_envio").toLocalDateTime()), um, outro, outro, um);
    }

    public void inserir(NovaMensagem nova) {
        jdbc.update("INSERT INTO mensagem (fk_email_remetente, fk_email_destinatario, texto_mensagem) VALUES (?, ?, ?)",
                nova.remetente(), nova.destinatario(), nova.texto());
    }
}
