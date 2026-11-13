package br.unirio.unilink.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.unirio.unilink.model.Conversa;
import br.unirio.unilink.model.Mensagem;
import br.unirio.unilink.model.NovaMensagem;
import br.unirio.unilink.service.MensagemService;

/**
 * Lista de conversas e mensagens privadas (UC04).
 */
@RestController
@RequestMapping("/api")
public class MensagemController {

    private final MensagemService service;

    public MensagemController(MensagemService service) {
        this.service = service;
    }

    // GET /api/conversas?email=... -> amigos com a ultima mensagem de cada conversa
    @GetMapping("/conversas")
    public List<Conversa> conversas(@RequestParam String email) {
        return service.conversas(email);
    }

    // GET /api/mensagens?email=...&com=... -> historico entre os dois
    @GetMapping("/mensagens")
    public List<Mensagem> historico(@RequestParam String email, @RequestParam String com) {
        return service.historico(email, com);
    }

    // POST /api/mensagens -> envia uma mensagem para um amigo
    @PostMapping("/mensagens")
    @ResponseStatus(HttpStatus.CREATED)
    public void enviar(@RequestBody NovaMensagem nova) {
        service.enviar(nova);
    }
}
