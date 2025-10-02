package com.desafio.votacao.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "votos",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_sessao_cpf",
        columnNames = {"sessao_votacao_id", "cpf_associado"}
    ),
    indexes = {
        @Index(name = "idx_sessao_id", columnList = "sessao_votacao_id"),
        @Index(name = "idx_cpf_associado", columnList = "cpf_associado")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_votacao_id", nullable = false)
    @NotNull(message = "Sessão de votação é obrigatória")
    private SessaoVotacao sessaoVotacao;

    @CPF(message = "CPF inválido")
    @NotBlank(message = "CPF é obrigatório")
    @Column(name = "cpf_associado", nullable = false, length = 11)
    private String cpfAssociado;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Opção de voto é obrigatória")
    @Column(nullable = false)
    private OpcaoVoto opcao;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @PrePersist
    protected void onCreate() {
        dataHora = LocalDateTime.now();
        // Remove formatação do CPF, mantém apenas números
        if (cpfAssociado != null) {
            cpfAssociado = cpfAssociado.replaceAll("[^0-9]", "");
        }
    }
}
