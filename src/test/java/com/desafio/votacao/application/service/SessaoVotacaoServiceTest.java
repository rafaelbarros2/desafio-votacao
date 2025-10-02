package com.desafio.votacao.application.service;

import com.desafio.votacao.application.dto.request.AbrirSessaoRequest;
import com.desafio.votacao.application.dto.response.ResultadoVotacaoResponse;
import com.desafio.votacao.application.dto.response.SessaoVotacaoResponse;
import com.desafio.votacao.domain.exception.PautaNaoEncontradaException;
import com.desafio.votacao.domain.exception.SessaoVotacaoNaoEncontradaException;
import com.desafio.votacao.domain.model.*;
import com.desafio.votacao.domain.repository.PautaRepository;
import com.desafio.votacao.domain.repository.SessaoVotacaoRepository;
import com.desafio.votacao.domain.repository.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessaoVotacaoService - Testes Unitários")
class SessaoVotacaoServiceTest {

    @Mock
    private SessaoVotacaoRepository sessaoRepository;

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private VotoRepository votoRepository;

    @InjectMocks
    private SessaoVotacaoService sessaoService;

    private Pauta pauta;
    private SessaoVotacao sessao;
    private AbrirSessaoRequest validRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sessaoService, "duracaoPadrao", 60);

        pauta = Pauta.builder()
                .id(UUID.randomUUID())
                .titulo("Pauta Teste")
                .descricao("Descrição da pauta")
                .dataCriacao(LocalDateTime.now())
                .build();

        sessao = SessaoVotacao.builder()
                .id(UUID.randomUUID())
                .pauta(pauta)
                .dataAbertura(LocalDateTime.now())
                .dataFechamento(LocalDateTime.now().plusSeconds(60))
                .status(StatusSessao.ABERTA)
                .duracaoSegundos(60)
                .build();

        validRequest = AbrirSessaoRequest.builder()
                .pautaId(pauta.getId())
                .duracaoSegundos(60)
                .build();
    }

    @Test
    @DisplayName("Deve abrir sessão de votação com sucesso")
    void deveAbrirSessaoComSucesso() {
        // Given
        when(pautaRepository.findById(pauta.getId())).thenReturn(Optional.of(pauta));
        when(sessaoRepository.save(any(SessaoVotacao.class))).thenReturn(sessao);

        // When
        SessaoVotacaoResponse response = sessaoService.abrirSessao(validRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(sessao.getId());
        assertThat(response.getPautaId()).isEqualTo(pauta.getId());
        assertThat(response.getStatus()).isEqualTo(StatusSessao.ABERTA);
        assertThat(response.getDuracaoSegundos()).isEqualTo(60);

        verify(pautaRepository, times(1)).findById(pauta.getId());
        verify(sessaoRepository, times(1)).save(any(SessaoVotacao.class));
    }

    @Test
    @DisplayName("Deve usar duração padrão quando não especificada")
    void deveUsarDuracaoPadraoQuandoNaoEspecificada() {
        // Given
        AbrirSessaoRequest requestSemDuracao = AbrirSessaoRequest.builder()
                .pautaId(pauta.getId())
                .build();

        when(pautaRepository.findById(pauta.getId())).thenReturn(Optional.of(pauta));
        when(sessaoRepository.save(any(SessaoVotacao.class))).thenReturn(sessao);

        // When
        SessaoVotacaoResponse response = sessaoService.abrirSessao(requestSemDuracao);

        // Then
        assertThat(response).isNotNull();
        verify(sessaoRepository).save(any(SessaoVotacao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando pauta não existe")
    void deveLancarExcecaoQuandoPautaNaoExiste() {
        // Given
        when(pautaRepository.findById(pauta.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sessaoService.abrirSessao(validRequest))
                .isInstanceOf(PautaNaoEncontradaException.class);

        verify(pautaRepository, times(1)).findById(pauta.getId());
        verify(sessaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando já existe sessão para a pauta")
    void deveLancarExcecaoQuandoJaExisteSessao() {
        // Given
        pauta.setSessaoVotacao(sessao);
        when(pautaRepository.findById(pauta.getId())).thenReturn(Optional.of(pauta));

        // When & Then
        assertThatThrownBy(() -> sessaoService.abrirSessao(validRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Já existe uma sessão");

        verify(pautaRepository, times(1)).findById(pauta.getId());
        verify(sessaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar sessão por ID com sucesso")
    void deveBuscarSessaoPorIdComSucesso() {
        // Given
        when(sessaoRepository.findById(sessao.getId())).thenReturn(Optional.of(sessao));

        // When
        SessaoVotacaoResponse response = sessaoService.buscarPorId(sessao.getId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(sessao.getId());

        verify(sessaoRepository, times(1)).findById(sessao.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando sessão não for encontrada")
    void deveLancarExcecaoQuandoSessaoNaoEncontrada() {
        // Given
        UUID sessaoId = UUID.randomUUID();
        when(sessaoRepository.findById(sessaoId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sessaoService.buscarPorId(sessaoId))
                .isInstanceOf(SessaoVotacaoNaoEncontradaException.class);

        verify(sessaoRepository, times(1)).findById(sessaoId);
    }

    @Test
    @DisplayName("Deve obter resultado da votação com sucesso")
    void deveObterResultadoComSucesso() {
        // Given
        when(sessaoRepository.findById(sessao.getId())).thenReturn(Optional.of(sessao));
        when(votoRepository.countBySessaoId(sessao.getId())).thenReturn(10L);
        when(votoRepository.countBySessaoAndOpcao(sessao.getId(), OpcaoVoto.SIM)).thenReturn(7L);
        when(votoRepository.countBySessaoAndOpcao(sessao.getId(), OpcaoVoto.NAO)).thenReturn(3L);

        // When
        ResultadoVotacaoResponse response = sessaoService.obterResultado(sessao.getId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSessaoId()).isEqualTo(sessao.getId());
        assertThat(response.getTotalVotos()).isEqualTo(10L);
        assertThat(response.getVotosSim()).isEqualTo(7L);
        assertThat(response.getVotosNao()).isEqualTo(3L);
        assertThat(response.getPercentualSim()).isEqualTo(70.0);
        assertThat(response.getPercentualNao()).isEqualTo(30.0);
        assertThat(response.getResultado()).isEqualTo("APROVADA");

        verify(sessaoRepository, times(1)).findById(sessao.getId());
        verify(votoRepository, times(1)).countBySessaoId(sessao.getId());
    }

    @Test
    @DisplayName("Deve retornar REJEITADA quando maioria votar NÃO")
    void deveRetornarRejeitadaQuandoMaioriaVotarNao() {
        // Given
        when(sessaoRepository.findById(sessao.getId())).thenReturn(Optional.of(sessao));
        when(votoRepository.countBySessaoId(sessao.getId())).thenReturn(10L);
        when(votoRepository.countBySessaoAndOpcao(sessao.getId(), OpcaoVoto.SIM)).thenReturn(3L);
        when(votoRepository.countBySessaoAndOpcao(sessao.getId(), OpcaoVoto.NAO)).thenReturn(7L);

        // When
        ResultadoVotacaoResponse response = sessaoService.obterResultado(sessao.getId());

        // Then
        assertThat(response.getResultado()).isEqualTo("REJEITADA");
    }

    @Test
    @DisplayName("Deve retornar EMPATE quando votos forem iguais")
    void deveRetornarEmpateQuandoVotosIguais() {
        // Given
        when(sessaoRepository.findById(sessao.getId())).thenReturn(Optional.of(sessao));
        when(votoRepository.countBySessaoId(sessao.getId())).thenReturn(10L);
        when(votoRepository.countBySessaoAndOpcao(sessao.getId(), OpcaoVoto.SIM)).thenReturn(5L);
        when(votoRepository.countBySessaoAndOpcao(sessao.getId(), OpcaoVoto.NAO)).thenReturn(5L);

        // When
        ResultadoVotacaoResponse response = sessaoService.obterResultado(sessao.getId());

        // Then
        assertThat(response.getResultado()).isEqualTo("EMPATE");
    }
}
