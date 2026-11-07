package br.unirio.unilink.service;

import static br.unirio.unilink.service.Validacao.exigir;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.unirio.unilink.model.Evento;
import br.unirio.unilink.repository.EstudanteRepository;
import br.unirio.unilink.repository.EventoRepository;

/**
 * Regras da agenda de eventos (UC06).
 */
@Service
public class EventoService {

    private final EventoRepository repository;
    private final EstudanteRepository estudantes;

    public EventoService(EventoRepository repository, EstudanteRepository estudantes) {
        this.repository = repository;
        this.estudantes = estudantes;
    }

    public List<Evento> listar(String email) {
        return repository.listar(email);
    }

    /** Grava a presenca e soma 1 no contador, as duas coisas juntas (transacao). */
    @Transactional
    public Evento confirmarPresenca(int id, String email) {
        exigir(email != null && estudantes.existe(email), "Estudante não encontrado");
        Evento evento = repository.buscar(id, email)
                .orElseThrow(() -> Validacao.naoEncontrado("Evento não encontrado"));
        // FE01: evento que ja aconteceu nao aceita confirmacao
        exigir(evento.dataHora().isAfter(LocalDateTime.now()), "Evento encerrado");
        try {
            repository.confirmarPresenca(id, email);
        } catch (DuplicateKeyException e) {
            throw Validacao.conflito("Você já confirmou presença neste evento");
        }
        return repository.buscar(id, email).orElseThrow();
    }
}
