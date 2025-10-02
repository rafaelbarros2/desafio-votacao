package com.desafio.votacao.application.service;

import com.desafio.votacao.application.dto.request.RegistrarVotoRequest;
import com.desafio.votacao.application.dto.response.VotoResponse;
import com.desafio.votacao.domain.exception.SessaoFechadaException;
import com.desafio.votacao.domain.exception.SessaoVotacaoNaoEncontradaException;
import com.desafio.votacao.domain.exception.VotoJaRegistradoException;
import com.desafio.votacao.domain.exception.CpfInvalidoException;
import com.desafio.votacao.domain.model.*;
import com.desafio.votacao.domain.repository.SessaoVotacaoRepository;
import com.desafio.votacao.domain.repository.VotoRepository;
import com.desafio.votacao.infrastructure.client.CpfValidationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VotoService - Testes Unitários")
class VotoServiceTest {

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private SessaoVotacaoRepository sessaoRepository;

    @Mock
    private CpfValidationClient cpfValidationClient;

    @InjectMocks
    private VotoService votoService;

    private SessaoVotacao sessaoAberta;
    private Pauta pauta;
    private Voto voto;
    private RegistrarVotoRequest validRequest;

    @BeforeEach
    void setUp() {
        pauta = Pauta.builder()
                .id(UUID.randomUUID())
                .titulo("Pauta Teste")
                .descricao("Descrição")
                .dataCriacao(LocalDateTime.now())
                .build();

        sessaoAberta = SessaoVotacao.builder()
                .id(UUID.randomUUID())
                .pauta(pauta)
                .dataAbertura(LocalDateTime.now())
                .dataFechamento(LocalDateTime.now().plusMinutes(5))
                .status(StatusSessao.ABERTA)
                .duracaoSegundos(300)
                .build();

        validRequest = RegistrarVotoRequest.builder()
                .sessaoId(sessaoAberta.getId())
                .cpf("12345678901")
                .opcao(OpcaoVoto.SIM)
                .build();

        voto = Voto.builder()
                .id(UUID.randomUUID())
                .sessaoVotacao(sessaoAberta)
                .cpfAssociado("12345678901")
                .opcao(OpcaoVoto.SIM)
                .dataHora(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve registrar voto com sucesso")
    void deveRegistrarVotoComSucesso() {
        // Given
        when(sessaoRepository.findById(sessaoAberta.getId())).thenReturn(Optional.of(sessaoAberta));
        when(cpfValidationClient.validarCpf("12345678901")).thenReturn(true);
        when(votoRepository.existsBySessaoVotacaoIdAndCpfAssociado(sessaoAberta.getId(), "12345678901"))
                .thenReturn(false);
        when(votoRepository.save(any(Voto.class))).thenReturn(voto);

        // When
        VotoResponse response = votoService.registrarVoto(validRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(voto.getId());
        assertThat(response.getSessaoId()).isEqualTo(sessaoAberta.getId());
        assertThat(response.getOpcao()).isEqualTo(OpcaoVoto.SIM);
        assertThat(response.getCpfMascarado()).isEqualTo("123.***.***-01");

        verify(sessaoRepository, times(1)).findById(sessaoAberta.getId());
        verify(cpfValidationClient, times(1)).validarCpf("12345678901");
        verify(votoRepository, times(1)).existsBySessaoVotacaoIdAndCpfAssociado(sessaoAberta.getId(), "12345678901");
        verify(votoRepository, times(1)).save(any(Voto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando sessão não existe")
    void deveLancarExcecaoQuandoSessaoNaoExiste() {
        // Given
        when(sessaoRepository.findById(sessaoAberta.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> votoService.registrarVoto(validRequest))
                .isInstanceOf(SessaoVotacaoNaoEncontradaException.class);

        verify(sessaoRepository, times(1)).findById(sessaoAberta.getId());
        verify(cpfValidationClient, never()).validarCpf(any());
        verify(votoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando sessão está fechada")
    void deveLancarExcecaoQuandoSessaoFechada() {
        // Given
        SessaoVotacao sessaoFechada = SessaoVotacao.builder()
                .id(UUID.randomUUID())
                .pauta(pauta)
                .dataAbertura(LocalDateTime.now().minusMinutes(10))
                .dataFechamento(LocalDateTime.now().minusMinutes(5))
                .status(StatusSessao.FECHADA)
                .duracaoSegundos(300)
                .build();

        when(sessaoRepository.findById(sessaoFechada.getId())).thenReturn(Optional.of(sessaoFechada));

        RegistrarVotoRequest request = RegistrarVotoRequest.builder()
                .sessaoId(sessaoFechada.getId())
                .cpf("12345678901")
                .opcao(OpcaoVoto.SIM)
                .build();

        // When & Then
        assertThatThrownBy(() -> votoService.registrarVoto(request))
                .isInstanceOf(SessaoFechadaException.class);

        verify(sessaoRepository, times(1)).findById(sessaoFechada.getId());
        verify(cpfValidationClient, never()).validarCpf(any());
        verify(votoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF não é válido")
    void deveLancarExcecaoQuandoCpfInvalido() {
        // Given
        when(sessaoRepository.findById(sessaoAberta.getId())).thenReturn(Optional.of(sessaoAberta));
        when(cpfValidationClient.validarCpf("12345678901"))
                .thenThrow(new CpfInvalidoException("12345678901"));

        // When & Then
        assertThatThrownBy(() -> votoService.registrarVoto(validRequest))
                .isInstanceOf(CpfInvalidoException.class);

        verify(sessaoRepository, times(1)).findById(sessaoAberta.getId());
        verify(cpfValidationClient, times(1)).validarCpf("12345678901");
        verify(votoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando associado já votou")
    void deveLancarExcecaoQuandoAssociadoJaVotou() {
        // Given
        when(sessaoRepository.findById(sessaoAberta.getId())).thenReturn(Optional.of(sessaoAberta));
        when(cpfValidationClient.validarCpf("12345678901")).thenReturn(true);
        when(votoRepository.existsBySessaoVotacaoIdAndCpfAssociado(sessaoAberta.getId(), "12345678901"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> votoService.registrarVoto(validRequest))
                .isInstanceOf(VotoJaRegistradoException.class);

        verify(sessaoRepository, times(1)).findById(sessaoAberta.getId());
        verify(cpfValidationClient, times(1)).validarCpf("12345678901");
        verify(votoRepository, times(1)).existsBySessaoVotacaoIdAndCpfAssociado(sessaoAberta.getId(), "12345678901");
        verify(votoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve tratar race condition ao salvar voto duplicado")
    void deveTratarRaceConditionAoSalvarVotoDuplicado() {
        // Given
        when(sessaoRepository.findById(sessaoAberta.getId())).thenReturn(Optional.of(sessaoAberta));
        when(cpfValidationClient.validarCpf("12345678901")).thenReturn(true);
        when(votoRepository.existsBySessaoVotacaoIdAndCpfAssociado(sessaoAberta.getId(), "12345678901"))
                .thenReturn(false);
        when(votoRepository.save(any(Voto.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        // When & Then
        assertThatThrownBy(() -> votoService.registrarVoto(validRequest))
                .isInstanceOf(VotoJaRegistradoException.class);

        verify(votoRepository, times(1)).save(any(Voto.class));
    }

    @Test
    @DisplayName("Deve limpar formatação do CPF antes de salvar")
    void deveLimparFormatacaoCpf() {
        // Given
        RegistrarVotoRequest requestComFormatacao = RegistrarVotoRequest.builder()
                .sessaoId(sessaoAberta.getId())
                .cpf("123.456.789-01")
                .opcao(OpcaoVoto.NAO)
                .build();

        when(sessaoRepository.findById(sessaoAberta.getId())).thenReturn(Optional.of(sessaoAberta));
        when(cpfValidationClient.validarCpf("12345678901")).thenReturn(true);
        when(votoRepository.existsBySessaoVotacaoIdAndCpfAssociado(sessaoAberta.getId(), "12345678901"))
                .thenReturn(false);
        when(votoRepository.save(any(Voto.class))).thenReturn(voto);

        // When
        VotoResponse response = votoService.registrarVoto(requestComFormatacao);

        // Then
        assertThat(response).isNotNull();
        verify(cpfValidationClient, times(1)).validarCpf("12345678901");
        verify(votoRepository, times(1)).existsBySessaoVotacaoIdAndCpfAssociado(sessaoAberta.getId(), "12345678901");
    }
}
