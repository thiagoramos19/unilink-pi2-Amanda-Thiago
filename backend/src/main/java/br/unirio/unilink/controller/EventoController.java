package br.unirio.unilink.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.unirio.unilink.model.Evento;
import br.unirio.unilink.model.Solicitante;
import br.unirio.unilink.service.EventoService;

/**
 * Agenda de eventos e confirmacao de presenca (UC06).
 */
@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService service;

    public EventoController(EventoService service) {
        this.service = service;
    }

    // GET /api/eventos?email=... -> eventos em ordem de data, marcando os que o estudante confirmou
    @GetMapping
    public List<Evento> listar(@RequestParam(required = false) String email) {
        return service.listar(email);
    }

    // POST /api/eventos/{id}/presenca -> confirma presenca e atualiza o contador
    @PostMapping("/{id}/presenca")
    public Evento confirmarPresenca(@PathVariable int id, @RequestBody Solicitante estudante) {
        return service.confirmarPresenca(id, estudante.email());
    }
}
