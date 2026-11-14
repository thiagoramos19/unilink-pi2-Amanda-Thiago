package br.unirio.unilink.service;

import static br.unirio.unilink.service.Validacao.exigir;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * Grava a presenca e soma 1 no contador, as duas coisas juntas (transacao).
     * Medicao 2: primeiro o UPDATE (trava a linha do evento), depois o INSERT.
     * Na medicao 1 era o contrario, e duas confirmacoes no mesmo evento entravam em deadlock.
     * As conferencias de "estudante existe" e "evento existe" sairam de consultas separadas
     * e passaram a ser feitas pelo proprio banco (WHERE do UPDATE e chave estrangeira do INSERT).
     */
    @Transactional
    public Evento confirmarPresenca(int id, String email) {
        exigir(email != null, "Estudante não encontrado");
        if (!repository.somarConfirmado(id)) {
            // FE01: evento que ja aconteceu nao aceita confirmacao
            if (repository.existe(id)) {
                throw Validacao.erro("Evento encerrado");
            }
            throw Validacao.naoEncontrado("Evento não encontrado");
        }
        try {
            repository.registrarPresenca(id, email);
        } catch (DuplicateKeyException e) {
            // a excecao desfaz a transacao inteira, inclusive o +1 do contador
            throw Validacao.conflito("Você já confirmou presença neste evento");
        } catch (DataIntegrityViolationException e) {
            throw Validacao.erro("Estudante não encontrado");
        }
        return repository.buscar(id, email).orElseThrow();
    }
}
