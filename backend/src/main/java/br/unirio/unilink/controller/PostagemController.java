package br.unirio.unilink.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.unirio.unilink.model.NovaPostagem;
import br.unirio.unilink.model.NovoComentario;
import br.unirio.unilink.model.Postagem;
import br.unirio.unilink.service.PostagemService;

/**
 * Porta de entrada do feed: recebe o pedido HTTP e devolve JSON.
 */
@RestController
@RequestMapping("/api/postagens")
public class PostagemController {

    private final PostagemService service;

    public PostagemController(PostagemService service) {
        this.service = service;
    }

    // GET /api/postagens -> lista as postagens com comentarios, da mais nova para a mais antiga (UC08)
    @GetMapping
    public List<Postagem> listar() {
        return service.listarFeed();
    }

    // POST /api/postagens -> publica uma postagem (UC09)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Postagem publicar(@RequestBody NovaPostagem nova) {
        return service.publicar(nova);
    }

    // POST /api/postagens/{id}/comentarios -> comenta uma postagem
    @PostMapping("/{id}/comentarios")
    @ResponseStatus(HttpStatus.CREATED)
    public void comentar(@PathVariable int id, @RequestBody NovoComentario novo) {
        service.comentar(id, novo);
    }
}
