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
public class SessaoVotacaoResponse {

    private UUID id;
    private UUID pautaId;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private StatusSessao status;
    private Integer duracaoSegundos;
}
