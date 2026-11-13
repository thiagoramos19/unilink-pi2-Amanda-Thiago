package br.unirio.unilink.service;

import static br.unirio.unilink.service.Validacao.exigir;

import java.util.List;

import org.springframework.stereotype.Service;

import br.unirio.unilink.model.NovoStory;
import br.unirio.unilink.model.Story;
import br.unirio.unilink.repository.EstudanteRepository;
import br.unirio.unilink.repository.StoryRepository;

/**
 * Regras dos stories (UC10).
 */
@Service
public class StoryService {

    private final StoryRepository repository;
    private final EstudanteRepository estudantes;

    public StoryService(StoryRepository repository, EstudanteRepository estudantes) {
        this.repository = repository;
        this.estudantes = estudantes;
    }

    public List<Story> listarAtivos() {
        return repository.listarAtivos();
    }

    public void publicar(NovoStory novo) {
        exigir(novo.emailAutor() != null && estudantes.existe(novo.emailAutor()), "Estudante não encontrado");
        repository.inserir(novo.emailAutor());
    }
}
