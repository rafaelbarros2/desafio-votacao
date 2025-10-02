package com.desafio.votacao.application.service;

import com.desafio.votacao.application.dto.request.AbrirSessaoRequest;
import com.desafio.votacao.application.dto.response.ResultadoVotacaoResponse;
import com.desafio.votacao.application.dto.response.SessaoVotacaoResponse;
import com.desafio.votacao.domain.exception.PautaNaoEncontradaException;
import com.desafio.votacao.domain.exception.SessaoVotacaoNaoEncontradaException;
import com.desafio.votacao.domain.model.OpcaoVoto;
import com.desafio.votacao.domain.model.Pauta;
import com.desafio.votacao.domain.model.SessaoVotacao;
import com.desafio.votacao.domain.repository.PautaRepository;
import com.desafio.votacao.domain.repository.SessaoVotacaoRepository;
import com.desafio.votacao.domain.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessaoVotacaoService {

    private final SessaoVotacaoRepository sessaoRepository;
    private final PautaRepository pautaRepository;
    private final VotoRepository votoRepository;

    @Value("${votacao.sessao.duracao-padrao:60}")
    private Integer duracaoPadrao;

    @Transactional
    public SessaoVotacaoResponse abrirSessao(AbrirSessaoRequest request) {
        log.info("Abrindo sessão de votação para pauta ID: {}", request.getPautaId());

        // Verifica se a pauta existe
        Pauta pauta = pautaRepository.findById(request.getPautaId())
                .orElseThrow(() -> new PautaNaoEncontradaException(request.getPautaId()));

        // Verifica se já existe sessão para esta pauta
        if (pauta.getSessaoVotacao() != null) {
            throw new IllegalStateException("Já existe uma sessão de votação para esta pauta");
        }

        // Define duração (usa valor informado ou padrão)
        Integer duracao = request.getDuracaoSegundos() != null
                ? request.getDuracaoSegundos()
                : duracaoPadrao;

        // Cria a sessão
        SessaoVotacao sessao = SessaoVotacao.builder()
                .pauta(pauta)
                .duracaoSegundos(duracao)
                .build();

        sessao = sessaoRepository.save(sessao);

        log.info("Sessão de votação aberta com sucesso. ID: {}, Duração: {}s, Fecha em: {}",
                sessao.getId(), duracao, sessao.getDataFechamento());

        return toResponse(sessao);
    }

    @Transactional(readOnly = true)
    public SessaoVotacaoResponse buscarPorId(UUID id) {
        log.info("Buscando sessão de votação por ID: {}", id);

        SessaoVotacao sessao = sessaoRepository.findById(id)
                .orElseThrow(() -> new SessaoVotacaoNaoEncontradaException(id));

        return toResponse(sessao);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "resultados", key = "#sessaoId")
    public ResultadoVotacaoResponse obterResultado(UUID sessaoId) {
        log.info("Contabilizando resultado da sessão: {}", sessaoId);

        SessaoVotacao sessao = sessaoRepository.findById(sessaoId)
                .orElseThrow(() -> new SessaoVotacaoNaoEncontradaException(sessaoId));

        // Contabiliza votos
        Long totalVotos = votoRepository.countBySessaoId(sessaoId);
        Long votosSim = votoRepository.countBySessaoAndOpcao(sessaoId, OpcaoVoto.SIM);
        Long votosNao = votoRepository.countBySessaoAndOpcao(sessaoId, OpcaoVoto.NAO);

        // Calcula percentuais
        Double percentualSim = totalVotos > 0 ? (votosSim * 100.0 / totalVotos) : 0.0;
        Double percentualNao = totalVotos > 0 ? (votosNao * 100.0 / totalVotos) : 0.0;

        // Define resultado
        String resultado;
        if (votosSim > votosNao) {
            resultado = "APROVADA";
        } else if (votosNao > votosSim) {
            resultado = "REJEITADA";
        } else {
            resultado = "EMPATE";
        }

        log.info("Resultado da sessão {}: Total={}, Sim={}, Não={}, Resultado={}",
                sessaoId, totalVotos, votosSim, votosNao, resultado);

        return ResultadoVotacaoResponse.builder()
                .sessaoId(sessao.getId())
                .pautaId(sessao.getPauta().getId())
                .tituloPauta(sessao.getPauta().getTitulo())
                .statusSessao(sessao.getStatus())
                .dataAbertura(sessao.getDataAbertura())
                .dataFechamento(sessao.getDataFechamento())
                .totalVotos(totalVotos)
                .votosSim(votosSim)
                .votosNao(votosNao)
                .percentualSim(percentualSim)
                .percentualNao(percentualNao)
                .resultado(resultado)
                .build();
    }

    private SessaoVotacaoResponse toResponse(SessaoVotacao sessao) {
        return SessaoVotacaoResponse.builder()
                .id(sessao.getId())
                .pautaId(sessao.getPauta().getId())
                .dataAbertura(sessao.getDataAbertura())
                .dataFechamento(sessao.getDataFechamento())
                .status(sessao.getStatus())
                .duracaoSegundos(sessao.getDuracaoSegundos())
                .build();
    }
}
