package br.unirio.unilink.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.unirio.unilink.model.NovoStory;
import br.unirio.unilink.model.Story;
import br.unirio.unilink.service.StoryService;

/**
 * Stories da tela inicial (UC10).
 */
@RestController
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService service;

    public StoryController(StoryService service) {
        this.service = service;
    }

    // GET /api/stories -> stories que ainda nao passaram de 24 horas
    @GetMapping
    public List<Story> listar() {
        return service.listarAtivos();
    }

    // POST /api/stories -> publica um story do estudante
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void publicar(@RequestBody NovoStory novo) {
        service.publicar(novo);
    }
}
