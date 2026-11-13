package br.unirio.unilink.service;

import static br.unirio.unilink.service.Validacao.exigir;

import java.util.List;

import org.springframework.stereotype.Service;

import br.unirio.unilink.model.Carta;
import br.unirio.unilink.model.NovaAmizade;
import br.unirio.unilink.repository.AmizadeRepository;
import br.unirio.unilink.repository.EstudanteRepository;

/**
 * Regras dos cards de descobrir e de adicionar amigo (UC03).
 */
@Service
public class AmizadeService {

    private static final int CARTAS_POR_RODADA = 3;

    private final AmizadeRepository repository;
    private final EstudanteRepository estudantes;

    public AmizadeService(AmizadeRepository repository, EstudanteRepository estudantes) {
        this.repository = repository;
        this.estudantes = estudantes;
    }

    public List<Carta> sortearCartas(String email) {
        return repository.sortearCartas(email, CARTAS_POR_RODADA);
    }

    // Pos-condicao do UC03: o vinculo e gravado e o chat fica liberado na hora,
    // por isso a amizade ja nasce com status ACEITO.
    public void adicionar(NovaAmizade amizade) {
        String eu = amizade.solicitante();
        String outro = amizade.solicitado();
        exigir(eu != null && estudantes.existe(eu), "Estudante não encontrado");
        exigir(outro != null && estudantes.existe(outro), "Estudante não encontrado");
        exigir(!eu.equals(outro), "Você não pode adicionar a si mesmo");
        if (repository.existeVinculo(eu, outro)) {
            throw Validacao.conflito("Vocês já são amigos");
        }
        repository.adicionar(eu, outro);
    }
}
