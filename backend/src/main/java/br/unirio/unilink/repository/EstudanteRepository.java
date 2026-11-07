package br.unirio.unilink.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import br.unirio.unilink.model.Biografia;
import br.unirio.unilink.model.Estudante;
import br.unirio.unilink.model.NovoEstudante;

/**
 * SQL das tabelas estudante e perfil.
 */
@Repository
public class EstudanteRepository {

    private static final RowMapper<Estudante> ESTUDANTE = (rs, linha) -> new Estudante(
            rs.getString("email_institucional"),
            rs.getString("nome"),
            rs.getDate("data_nascimento").toLocalDate(),
            rs.getString("curso"),
            rs.getString("campus_predio"));

    private final JdbcTemplate jdbc;

    public EstudanteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existe(String email) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM estudante WHERE email_institucional = ?", Integer.class, email);
        return total > 0;
    }

    public void inserir(NovoEstudante novo, String senhaHash) {
        jdbc.update("""
                INSERT INTO estudante (email_institucional, nome, data_nascimento, curso, campus_predio, senha_hash)
                VALUES (?, ?, ?, ?, ?, ?)
                """, novo.email(), novo.nome(), novo.dataNascimento(), novo.curso(), novo.campus(), senhaHash);
        jdbc.update("""
                INSERT INTO perfil (fk_email_estudante, tres_gostos, frase_marcante, mood_atual, funfact)
                VALUES (?, ?, ?, ?, ?)
                """, novo.email(), novo.gostos(), novo.frase(), novo.mood(), novo.funfact());
    }

    public Optional<Estudante> buscarPorCredenciais(String email, String senhaHash) {
        List<Estudante> encontrados = jdbc.query("""
                SELECT email_institucional, nome, data_nascimento, curso, campus_predio
                FROM estudante
                WHERE email_institucional = ? AND senha_hash = ?
                """, ESTUDANTE, email, senhaHash);
        return encontrados.stream().findFirst();
    }

    public Optional<Estudante> buscar(String email) {
        List<Estudante> encontrados = jdbc.query("""
                SELECT email_institucional, nome, data_nascimento, curso, campus_predio
                FROM estudante
                WHERE email_institucional = ?
                """, ESTUDANTE, email);
        return encontrados.stream().findFirst();
    }

    public Biografia buscarBiografia(String email) {
        List<Biografia> encontradas = jdbc.query("""
                SELECT tres_gostos, frase_marcante, mood_atual, funfact
                FROM perfil
                WHERE fk_email_estudante = ?
                """, (rs, linha) -> new Biografia(
                        rs.getString("tres_gostos"),
                        rs.getString("frase_marcante"),
                        rs.getString("mood_atual"),
                        rs.getString("funfact")), email);
        return encontradas.stream().findFirst().orElse(new Biografia(null, null, null, null));
    }

    public void salvarBiografia(String email, Biografia bio) {
        int alteradas = jdbc.update("""
                UPDATE perfil
                SET tres_gostos = ?, frase_marcante = ?, mood_atual = ?, funfact = ?
                WHERE fk_email_estudante = ?
                """, bio.gostos(), bio.frase(), bio.mood(), bio.funfact(), email);
        if (alteradas == 0) {
            jdbc.update("""
                    INSERT INTO perfil (fk_email_estudante, tres_gostos, frase_marcante, mood_atual, funfact)
                    VALUES (?, ?, ?, ?, ?)
                    """, email, bio.gostos(), bio.frase(), bio.mood(), bio.funfact());
        }
    }

    public int contarAmigos(String email) {
        return jdbc.queryForObject("""
                SELECT COUNT(*) FROM solicitacao_amizade
                WHERE status = 'ACEITO' AND (fk_email_solicitante = ? OR fk_email_solicitado = ?)
                """, Integer.class, email, email);
    }

    public int contarEventos(String email) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM presenca_evento WHERE fk_email_estudante = ?", Integer.class, email);
    }
}
