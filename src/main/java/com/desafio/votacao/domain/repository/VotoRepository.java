package com.desafio.votacao.domain.repository;

import com.desafio.votacao.domain.model.OpcaoVoto;
import com.desafio.votacao.domain.model.Voto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VotoRepository extends JpaRepository<Voto, UUID> {

    Optional<Voto> findBySessaoVotacaoIdAndCpfAssociado(UUID sessaoId, String cpf);

    boolean existsBySessaoVotacaoIdAndCpfAssociado(UUID sessaoId, String cpf);

    List<Voto> findBySessaoVotacaoId(UUID sessaoId);

    @Query("SELECT COUNT(v) FROM Voto v WHERE v.sessaoVotacao.id = :sessaoId AND v.opcao = :opcao")
    Long countBySessaoAndOpcao(UUID sessaoId, OpcaoVoto opcao);

    @Query("SELECT COUNT(v) FROM Voto v WHERE v.sessaoVotacao.id = :sessaoId")
    Long countBySessaoId(UUID sessaoId);
}
