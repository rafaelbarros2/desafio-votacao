package com.desafio.votacao.application.dto.request;

import com.desafio.votacao.domain.model.OpcaoVoto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.br.CPF;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarVotoRequest {

    @NotNull(message = "ID da sessão é obrigatório")
    private UUID sessaoId;

    @NotBlank(message = "CPF é obrigatório")
    @CPF(message = "CPF inválido")
    private String cpf;

    @NotNull(message = "Opção de voto é obrigatória")
    private OpcaoVoto opcao;
}
