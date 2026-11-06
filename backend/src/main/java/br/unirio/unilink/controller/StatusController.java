package br.unirio.unilink.controller;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /api/status -> confere se o back-end esta no ar e conversa com o banco.
 */
@RestController
public class StatusController {

    private final JdbcTemplate jdbc;

    public StatusController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/status")
    public Map<String, Object> status() {
        Integer estudantes = jdbc.queryForObject("SELECT COUNT(*) FROM estudante", Integer.class);
        return Map.of("backend", "ok", "banco", "ok", "estudantes", estudantes);
    }
}
