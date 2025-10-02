package com.desafio.votacao.application.dto.response;

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
public class PautaResponse {

    private UUID id;
    private String titulo;
    private String descricao;
    private LocalDateTime dataCriacao;
    private SessaoVotacaoResponse sessaoVotacao;
}
