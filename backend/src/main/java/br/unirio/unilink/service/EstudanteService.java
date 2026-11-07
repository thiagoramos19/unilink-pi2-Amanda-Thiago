package br.unirio.unilink.service;

import static br.unirio.unilink.service.Validacao.DOMINIO;
import static br.unirio.unilink.service.Validacao.exigir;
import static br.unirio.unilink.service.Validacao.vazio;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.unirio.unilink.model.Biografia;
import br.unirio.unilink.model.Estudante;
import br.unirio.unilink.model.Login;
import br.unirio.unilink.model.NovoEstudante;
import br.unirio.unilink.model.Perfil;
import br.unirio.unilink.repository.EstudanteRepository;

/**
 * Regras de cadastro (UC01), login (UC02) e perfil (UC07).
 */
@Service
public class EstudanteService {

    private final EstudanteRepository repository;

    public EstudanteService(EstudanteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Estudante cadastrar(NovoEstudante novo) {
        exigir(novo.email() != null && novo.email().endsWith(DOMINIO), "Email fora do formato " + DOMINIO);
        exigir(novo.senha() != null && novo.senha().length() >= 6, "A senha precisa de pelo menos 6 caracteres");
        exigir(!vazio(novo.nome()), "Digite seu nome!");
        exigir(novo.dataNascimento() != null, "Digite uma data de nascimento!");
        exigir(!vazio(novo.curso()), "Digite seu curso!");
        exigir(!vazio(novo.campus()), "Escolha seu campus!");
        if (repository.existe(novo.email())) {
            throw Validacao.conflito("Este e-mail já tem uma conta");
        }
        repository.inserir(novo, Senhas.hash(novo.senha()));
        return repository.buscar(novo.email()).orElseThrow();
    }

    public Estudante entrar(Login login) {
        if (login.email() == null || login.senha() == null) {
            throw credenciaisIncorretas();
        }
        return repository.buscarPorCredenciais(login.email(), Senhas.hash(login.senha()))
                .orElseThrow(EstudanteService::credenciaisIncorretas);
    }

    // FA02 do UC02: so confere se o e-mail existe (o envio de e-mail esta fora do escopo)
    public void recuperarSenha(String email) {
        if (email == null || !repository.existe(email)) {
            throw Validacao.naoEncontrado("Não encontramos uma conta com esse e-mail");
        }
    }

    public Perfil perfil(String email) {
        Estudante estudante = repository.buscar(email)
                .orElseThrow(() -> Validacao.naoEncontrado("Estudante não encontrado"));
        return new Perfil(
                estudante,
                repository.buscarBiografia(email),
                repository.contarAmigos(email),
                repository.contarEventos(email));
    }

    public Biografia salvarBiografia(String email, Biografia bio) {
        exigir(repository.existe(email), "Estudante não encontrado");
        exigir(cabe(bio.gostos(), 255) && cabe(bio.frase(), 255) && cabe(bio.mood(), 100) && cabe(bio.funfact(), 255),
                "Limite de caracteres atingido");
        repository.salvarBiografia(email, bio);
        return bio;
    }

    private static boolean cabe(String texto, int limite) {
        return texto == null || texto.length() <= limite;
    }

    private static ResponseStatusException credenciaisIncorretas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha incorretos");
    }
}
