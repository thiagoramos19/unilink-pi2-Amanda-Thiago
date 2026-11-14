package br.unirio.unilink.service;

import static br.unirio.unilink.service.Validacao.exigir;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import br.unirio.unilink.model.Evento;
import br.unirio.unilink.repository.EventoRepository;

/**
 * Regras da agenda de eventos (UC06).
 */
@Service
public class EventoService {

    private final EventoRepository repository;

    public EventoService(EventoRepository repository) {
        this.repository = repository;
    }

    public List<Evento> listar(String email) {
        return repository.listar(email);
    }

    /**
     * Grava a presenca (UC06).
     * Medicao 1: INSERT da presenca + UPDATE do contador na linha do evento, na mesma
     * transacao. Duas confirmacoes no mesmo evento entravam em deadlock.
     * Medicao 2: so o INSERT. O numero de confirmados e contado em presenca_evento,
     * entao ninguem escreve na linha do evento e as confirmacoes nao fazem fila.
     * A chave primaria impede presenca repetida e a chave estrangeira, estudante inexistente.
     */
    public Evento confirmarPresenca(int id, String email) {
        exigir(email != null, "Estudante não encontrado");
        Evento evento = repository.buscar(id, email)
                .orElseThrow(() -> Validacao.naoEncontrado("Evento não encontrado"));
        // FE01: evento que ja aconteceu nao aceita confirmacao
        exigir(evento.dataHora().isAfter(LocalDateTime.now()), "Evento encerrado");
        try {
            repository.registrarPresenca(id, email);
        } catch (DuplicateKeyException e) {
            throw Validacao.conflito("Você já confirmou presença neste evento");
        } catch (DataIntegrityViolationException e) {
            throw Validacao.erro("Estudante não encontrado");
        }
        return repository.buscar(id, email).orElseThrow();
    }
}
