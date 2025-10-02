package com.desafio.votacao.application.service;

import com.desafio.votacao.application.dto.request.RegistrarVotoRequest;
import com.desafio.votacao.application.dto.response.VotoResponse;
import com.desafio.votacao.domain.exception.SessaoFechadaException;
import com.desafio.votacao.domain.exception.SessaoVotacaoNaoEncontradaException;
import com.desafio.votacao.domain.exception.VotoJaRegistradoException;
import com.desafio.votacao.domain.model.SessaoVotacao;
import com.desafio.votacao.domain.model.Voto;
import com.desafio.votacao.domain.repository.SessaoVotacaoRepository;
import com.desafio.votacao.domain.repository.VotoRepository;
import com.desafio.votacao.infrastructure.client.CpfValidationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VotoService {

    private final VotoRepository votoRepository;
    private final SessaoVotacaoRepository sessaoRepository;
    private final CpfValidationClient cpfValidationClient;

    @Transactional
    public VotoResponse registrarVoto(RegistrarVotoRequest request) {
        String cpfLimpo = request.getCpf().replaceAll("[^0-9]", "");
        log.info("Registrando voto - Sessão: {}, CPF: {}, Opção: {}",
                request.getSessaoId(), maskCpf(cpfLimpo), request.getOpcao());

        // 1. Busca a sessão
        SessaoVotacao sessao = sessaoRepository.findById(request.getSessaoId())
                .orElseThrow(() -> new SessaoVotacaoNaoEncontradaException(request.getSessaoId()));

        // 2. Valida se a sessão está aberta
        if (!sessao.isAberta()) {
            log.warn("Tentativa de voto em sessão fechada: {}", request.getSessaoId());
            throw new SessaoFechadaException(request.getSessaoId());
        }

        // 3. Valida CPF (Bônus 1)
        cpfValidationClient.validarCpf(cpfLimpo);

        // 4. Verifica se já votou
        if (votoRepository.existsBySessaoVotacaoIdAndCpfAssociado(request.getSessaoId(), cpfLimpo)) {
            log.warn("Voto duplicado detectado - Sessão: {}, CPF: {}",
                    request.getSessaoId(), maskCpf(cpfLimpo));
            throw new VotoJaRegistradoException(cpfLimpo, request.getSessaoId());
        }

        // 5. Cria e salva o voto
        Voto voto = Voto.builder()
                .sessaoVotacao(sessao)
                .cpfAssociado(cpfLimpo)
                .opcao(request.getOpcao())
                .build();

        try {
            voto = votoRepository.save(voto);
            log.info("Voto registrado com sucesso - ID: {}, Sessão: {}, Opção: {}",
                    voto.getId(), request.getSessaoId(), request.getOpcao());
        } catch (DataIntegrityViolationException e) {
            // Race condition: outro voto foi registrado entre a verificação e o save
            log.error("Erro de integridade ao registrar voto (race condition) - Sessão: {}, CPF: {}",
                    request.getSessaoId(), maskCpf(cpfLimpo));
            throw new VotoJaRegistradoException(cpfLimpo, request.getSessaoId());
        }

        return toResponse(voto);
    }

    private VotoResponse toResponse(Voto voto) {
        return VotoResponse.builder()
                .id(voto.getId())
                .sessaoId(voto.getSessaoVotacao().getId())
                .cpfMascarado(maskCpf(voto.getCpfAssociado()))
                .opcao(voto.getOpcao())
                .dataHora(voto.getDataHora())
                .build();
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) {
            return "***";
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }
}
