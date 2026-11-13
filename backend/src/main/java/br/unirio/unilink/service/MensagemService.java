package br.unirio.unilink.service;

import static br.unirio.unilink.service.Validacao.exigir;
import static br.unirio.unilink.service.Validacao.vazio;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.unirio.unilink.model.Conversa;
import br.unirio.unilink.model.Mensagem;
import br.unirio.unilink.model.NovaMensagem;
import br.unirio.unilink.repository.AmizadeRepository;
import br.unirio.unilink.repository.MensagemRepository;

/**
 * Regras do chat (UC04): so amigos conversam entre si.
 */
@Service
public class MensagemService {

    private final MensagemRepository repository;
    private final AmizadeRepository amizades;

    public MensagemService(MensagemRepository repository, AmizadeRepository amizades) {
        this.repository = repository;
        this.amizades = amizades;
    }

    public List<Conversa> conversas(String email) {
        return repository.listarConversas(email);
    }

    public List<Mensagem> historico(String email, String com) {
        exigirAmizade(email, com);
        return repository.listarHistorico(email, com);
    }

    public void enviar(NovaMensagem nova) {
        // FE01: mensagem em branco nao e enviada
        exigir(!vazio(nova.texto()), "Escreva uma mensagem");
        exigirAmizade(nova.remetente(), nova.destinatario());
        repository.inserir(new NovaMensagem(nova.remetente(), nova.destinatario(), nova.texto().trim()));
    }

    private void exigirAmizade(String um, String outro) {
        if (um == null || outro == null || !amizades.saoAmigos(um, outro)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vocês precisam ser amigos para conversar");
        }
    }
}
