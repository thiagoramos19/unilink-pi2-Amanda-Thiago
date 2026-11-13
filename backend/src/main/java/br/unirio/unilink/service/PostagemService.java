package br.unirio.unilink.service;

import static br.unirio.unilink.service.Validacao.exigir;
import static br.unirio.unilink.service.Validacao.vazio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.unirio.unilink.model.Estudante;
import br.unirio.unilink.model.NovaPostagem;
import br.unirio.unilink.model.NovoComentario;
import br.unirio.unilink.model.Postagem;
import br.unirio.unilink.repository.EstudanteRepository;
import br.unirio.unilink.repository.PostagemRepository;

/**
 * Regras de negocio do feed (UC08 e UC09).
 */
@Service
public class PostagemService {

    private static final Set<String> TIPOS_MIDIA = Set.of("IMAGEM", "MUSICA", "LIVRO", "ARQUIVO", "NENHUMA");

    private final PostagemRepository repository;
    private final EstudanteRepository estudantes;

    public PostagemService(PostagemRepository repository, EstudanteRepository estudantes) {
        this.repository = repository;
        this.estudantes = estudantes;
    }

    /** Todas as postagens, da mais nova para a mais antiga, cada uma com seus comentarios. */
    public List<Postagem> listarFeed() {
        return repository.listarTodas().stream()
                .map(p -> new Postagem(p.id(), p.emailAutor(), p.nomeAutor(), p.cursoAutor(), p.campusAutor(),
                        p.texto(), p.tipoMidia(), p.dataHoraCriacao(), repository.listarComentarios(p.id())))
                .toList();
    }

    public Postagem publicar(NovaPostagem nova) {
        String tipo = nova.tipoMidia() == null ? "NENHUMA" : nova.tipoMidia();
        exigir(TIPOS_MIDIA.contains(tipo), "Tipo de mídia inválido");
        // FE01 do UC09: nao aceita postagem sem texto e sem midia
        exigir(!vazio(nova.texto()) || !tipo.equals("NENHUMA"), "Escreva algo ou anexe uma mídia para postar");
        Estudante autor = nova.emailAutor() == null ? null : estudantes.buscar(nova.emailAutor()).orElse(null);
        exigir(autor != null, "Autor não encontrado");

        String texto = vazio(nova.texto()) ? null : nova.texto().trim();
        int id = repository.inserir(new NovaPostagem(autor.email(), texto, tipo));
        return new Postagem(id, autor.email(), autor.nome(), autor.curso(), autor.campus(),
                texto, tipo, LocalDateTime.now(), List.of());
    }

    public void comentar(int idPostagem, NovoComentario novo) {
        exigir(!vazio(novo.texto()), "Escreva o comentário");
        exigir(novo.texto().length() <= 255, "O comentário pode ter até 255 caracteres");
        exigir(novo.emailAutor() != null && estudantes.existe(novo.emailAutor()), "Autor não encontrado");
        if (!repository.existe(idPostagem)) {
            throw Validacao.naoEncontrado("Postagem não encontrada");
        }
        repository.inserirComentario(idPostagem, new NovoComentario(novo.emailAutor(), novo.texto().trim()));
    }
}
