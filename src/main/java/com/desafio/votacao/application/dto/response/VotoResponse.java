package com.desafio.votacao.application.dto.response;

import com.desafio.votacao.domain.model.OpcaoVoto;
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
public class VotoResponse {

    private UUID id;
    private UUID sessaoId;
    private String cpfMascarado;
    private OpcaoVoto opcao;
    private LocalDateTime dataHora;
}
