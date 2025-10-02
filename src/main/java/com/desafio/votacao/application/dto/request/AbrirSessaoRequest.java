package com.desafio.votacao.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbrirSessaoRequest {

    @NotNull(message = "ID da pauta é obrigatório")
    private UUID pautaId;

    @Min(value = 1, message = "Duração deve ser de pelo menos 1 segundo")
    private Integer duracaoSegundos; // null = usa padrão de 60 segundos
}
