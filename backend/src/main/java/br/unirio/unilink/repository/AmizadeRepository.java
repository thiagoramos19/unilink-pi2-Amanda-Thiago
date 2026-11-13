package br.unirio.unilink.repository;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.unirio.unilink.model.Biografia;
import br.unirio.unilink.model.Carta;

/**
 * SQL da tabela solicitacao_amizade e das cartas da tela Descobrir.
 */
@Repository
public class AmizadeRepository {

    private final JdbcTemplate jdbc;

    public AmizadeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Estudantes aleatorios que ainda nao tem amizade com quem pediu. */
    public List<Carta> sortearCartas(String email, int quantidade) {
        String sql = """
                SELECT e.email_institucional, e.nome, e.data_nascimento, e.curso, e.campus_predio,
                       p.tres_gostos, p.frase_marcante, p.mood_atual, p.funfact
                FROM estudante e
                LEFT JOIN perfil p ON p.fk_email_estudante = e.email_institucional
                WHERE e.email_institucional <> ?
                  AND NOT EXISTS (
                      SELECT 1 FROM solicitacao_amizade s
                      WHERE (s.fk_email_solicitante = ? AND s.fk_email_solicitado = e.email_institucional)
                         OR (s.fk_email_solicitado = ? AND s.fk_email_solicitante = e.email_institucional))
                ORDER BY RAND()
                LIMIT ?
                """;
        return jdbc.query(sql, (rs, linha) -> new Carta(
                rs.getString("email_institucional"),
                rs.getString("nome"),
                Period.between(rs.getDate("data_nascimento").toLocalDate(), LocalDate.now()).getYears(),
                rs.getString("curso"),
                rs.getString("campus_predio"),
                new Biografia(
                        rs.getString("tres_gostos"),
                        rs.getString("frase_marcante"),
                        rs.getString("mood_atual"),
                        rs.getString("funfact"))), email, email, email, quantidade);
    }

    /** Existe algum pedido de amizade entre os dois, em qualquer sentido? */
    public boolean existeVinculo(String um, String outro) {
        Integer total = jdbc.queryForObject("""
                SELECT COUNT(*) FROM solicitacao_amizade
                WHERE (fk_email_solicitante = ? AND fk_email_solicitado = ?)
                   OR (fk_email_solicitante = ? AND fk_email_solicitado = ?)
                """, Integer.class, um, outro, outro, um);
        return total > 0;
    }

    /** Amizade aceita entre os dois, em qualquer sentido (exigida para o chat). */
    public boolean saoAmigos(String um, String outro) {
        Integer total = jdbc.queryForObject("""
                SELECT COUNT(*) FROM solicitacao_amizade
                WHERE status = 'ACEITO'
                  AND ((fk_email_solicitante = ? AND fk_email_solicitado = ?)
                    OR (fk_email_solicitante = ? AND fk_email_solicitado = ?))
                """, Integer.class, um, outro, outro, um);
        return total > 0;
    }

    public void adicionar(String solicitante, String solicitado) {
        jdbc.update("""
                INSERT INTO solicitacao_amizade (fk_email_solicitante, fk_email_solicitado, status, data_confirmacao)
                VALUES (?, ?, 'ACEITO', NOW())
                """, solicitante, solicitado);
    }
}
