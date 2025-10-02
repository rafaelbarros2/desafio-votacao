package com.desafio.votacao.application.dto.response;

import com.desafio.votacao.domain.model.StatusSessao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoVotacaoResponse {

    private UUID sessaoId;
    private UUID pautaId;
    private String tituloPauta;
    private StatusSessao statusSessao;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private Long totalVotos;
    private Long votosSim;
    private Long votosNao;
    private Double percentualSim;
    private Double percentualNao;
    private String resultado; // "APROVADA", "REJEITADA", "EMPATE"
}
