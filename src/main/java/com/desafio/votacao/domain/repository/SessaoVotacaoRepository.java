package com.desafio.votacao.domain.repository;

import com.desafio.votacao.domain.model.SessaoVotacao;
import com.desafio.votacao.domain.model.StatusSessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessaoVotacaoRepository extends JpaRepository<SessaoVotacao, UUID> {

    Optional<SessaoVotacao> findByPautaId(UUID pautaId);

    List<SessaoVotacao> findByStatusAndDataFechamentoBefore(StatusSessao status, LocalDateTime dataFechamento);

    @Query("SELECT s FROM SessaoVotacao s WHERE s.status = :status")
    List<SessaoVotacao> findByStatus(StatusSessao status);
}
