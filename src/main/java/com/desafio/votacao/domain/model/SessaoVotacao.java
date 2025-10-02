package com.desafio.votacao.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sessoes_votacao")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessaoVotacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "pauta_id", nullable = false, unique = true)
    private Pauta pauta;

    @Column(name = "data_abertura", nullable = false, updatable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_fechamento", nullable = false)
    private LocalDateTime dataFechamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSessao status;

    @Column(name = "duracao_segundos", nullable = false)
    private Integer duracaoSegundos;

    @OneToMany(mappedBy = "sessaoVotacao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Voto> votos = new ArrayList<>();

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        dataAbertura = LocalDateTime.now();
        if (duracaoSegundos == null) {
            duracaoSegundos = 60; // Default 1 minute
        }
        dataFechamento = dataAbertura.plusSeconds(duracaoSegundos);
        status = StatusSessao.ABERTA;
    }

    public boolean isAberta() {
        return status == StatusSessao.ABERTA && LocalDateTime.now().isBefore(dataFechamento);
    }

    public void fechar() {
        this.status = StatusSessao.FECHADA;
    }
}
