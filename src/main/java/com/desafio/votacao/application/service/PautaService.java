package com.desafio.votacao.application.service;

import com.desafio.votacao.application.dto.request.CriarPautaRequest;
import com.desafio.votacao.application.dto.response.PautaResponse;
import com.desafio.votacao.application.dto.response.SessaoVotacaoResponse;
import com.desafio.votacao.domain.exception.PautaNaoEncontradaException;
import com.desafio.votacao.domain.model.Pauta;
import com.desafio.votacao.domain.repository.PautaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PautaService {

    private final PautaRepository pautaRepository;

    @Transactional
    public PautaResponse criarPauta(CriarPautaRequest request) {
        log.info("Criando nova pauta: {}", request.getTitulo());

        Pauta pauta = Pauta.builder()
                .titulo(request.getTitulo())
                .descricao(request.getDescricao())
                .build();

        pauta = pautaRepository.save(pauta);

        log.info("Pauta criada com sucesso. ID: {}", pauta.getId());

        return toResponse(pauta);
    }

    @Transactional(readOnly = true)
    public PautaResponse buscarPorId(UUID id) {
        log.info("Buscando pauta por ID: {}", id);

        Pauta pauta = pautaRepository.findById(id)
                .orElseThrow(() -> new PautaNaoEncontradaException(id));

        return toResponse(pauta);
    }

    @Transactional(readOnly = true)
    public List<PautaResponse> listarTodas() {
        log.info("Listando todas as pautas");

        return pautaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PautaResponse toResponse(Pauta pauta) {
        SessaoVotacaoResponse sessaoResponse = null;

        if (pauta.getSessaoVotacao() != null) {
            sessaoResponse = SessaoVotacaoResponse.builder()
                    .id(pauta.getSessaoVotacao().getId())
                    .pautaId(pauta.getId())
                    .dataAbertura(pauta.getSessaoVotacao().getDataAbertura())
                    .dataFechamento(pauta.getSessaoVotacao().getDataFechamento())
                    .status(pauta.getSessaoVotacao().getStatus())
                    .duracaoSegundos(pauta.getSessaoVotacao().getDuracaoSegundos())
                    .build();
        }

        return PautaResponse.builder()
                .id(pauta.getId())
                .titulo(pauta.getTitulo())
                .descricao(pauta.getDescricao())
                .dataCriacao(pauta.getDataCriacao())
                .sessaoVotacao(sessaoResponse)
                .build();
    }
}
