package br.unirio.unilink.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.unirio.unilink.model.Biografia;
import br.unirio.unilink.model.Estudante;
import br.unirio.unilink.model.Login;
import br.unirio.unilink.model.NovoEstudante;
import br.unirio.unilink.model.Perfil;
import br.unirio.unilink.service.EstudanteService;

/**
 * Cadastro, login, recuperar senha e perfil.
 */
@RestController
@RequestMapping("/api")
public class EstudanteController {

    private final EstudanteService service;

    public EstudanteController(EstudanteService service) {
        this.service = service;
    }

    // POST /api/estudantes -> cria a conta (UC01)
    @PostMapping("/estudantes")
    @ResponseStatus(HttpStatus.CREATED)
    public Estudante cadastrar(@RequestBody NovoEstudante novo) {
        return service.cadastrar(novo);
    }

    // POST /api/login -> confere e-mail e senha (UC02)
    @PostMapping("/login")
    public Estudante entrar(@RequestBody Login login) {
        return service.entrar(login);
    }

    // POST /api/recuperar-senha -> confere se o e-mail tem conta (UC02, FA02)
    @PostMapping("/recuperar-senha")
    public Map<String, String> recuperarSenha(@RequestBody Map<String, String> corpo) {
        service.recuperarSenha(corpo.get("email"));
        return Map.of("mensagem", "Enviamos as instruções para o seu e-mail");
    }

    // GET /api/estudantes/{email}/perfil -> dados, biografia e contadores (UC07)
    @GetMapping("/estudantes/{email}/perfil")
    public Perfil perfil(@PathVariable String email) {
        return service.perfil(email);
    }

    // PUT /api/estudantes/{email}/biografia -> salva a biografia editada (UC07)
    @PutMapping("/estudantes/{email}/biografia")
    public Biografia salvarBiografia(@PathVariable String email, @RequestBody Biografia bio) {
        return service.salvarBiografia(email, bio);
    }
}
