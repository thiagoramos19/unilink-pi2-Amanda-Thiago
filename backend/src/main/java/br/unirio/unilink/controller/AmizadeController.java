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

import br.unirio.unilink.model.Carta;
import br.unirio.unilink.model.NovaAmizade;
import br.unirio.unilink.service.AmizadeService;

/**
 * Cards de descobrir e adicionar amigo (UC03).
 */
@RestController
@RequestMapping("/api")
public class AmizadeController {

    private final AmizadeService service;

    public AmizadeController(AmizadeService service) {
        this.service = service;
    }

    // GET /api/cartas?email=... -> ate 3 estudantes aleatorios que ainda nao sao amigos
    @GetMapping("/cartas")
    public List<Carta> sortearCartas(@RequestParam String email) {
        return service.sortearCartas(email);
    }

    // POST /api/amizades -> adiciona o amigo e libera o chat
    @PostMapping("/amizades")
    @ResponseStatus(HttpStatus.CREATED)
    public void adicionar(@RequestBody NovaAmizade amizade) {
        service.adicionar(amizade);
    }
}
