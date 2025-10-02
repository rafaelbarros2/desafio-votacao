package com.desafio.votacao.infrastructure.config;

import com.desafio.votacao.domain.model.SessaoVotacao;
import com.desafio.votacao.domain.model.StatusSessao;
import com.desafio.votacao.domain.repository.SessaoVotacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessaoVotacaoScheduler {

    private final SessaoVotacaoRepository sessaoRepository;

    /**
     * Job que fecha automaticamente sessões expiradas
     * Executa a cada 10 segundos
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void fecharSessoesExpiradas() {
        LocalDateTime agora = LocalDateTime.now();

        List<SessaoVotacao> sessoesExpiradas = sessaoRepository
                .findByStatusAndDataFechamentoBefore(StatusSessao.ABERTA, agora);

        if (!sessoesExpiradas.isEmpty()) {
            log.info("Fechando {} sessões expiradas", sessoesExpiradas.size());

            sessoesExpiradas.forEach(sessao -> {
                sessao.fechar();
                sessaoRepository.save(sessao);
                log.info("Sessão {} fechada automaticamente. Pauta: {}",
                        sessao.getId(), sessao.getPauta().getTitulo());
            });
        }
    }
}
