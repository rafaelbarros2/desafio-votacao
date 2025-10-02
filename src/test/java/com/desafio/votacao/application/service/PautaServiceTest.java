package com.desafio.votacao.application.service;

import com.desafio.votacao.application.dto.request.CriarPautaRequest;
import com.desafio.votacao.application.dto.response.PautaResponse;
import com.desafio.votacao.domain.exception.PautaNaoEncontradaException;
import com.desafio.votacao.domain.model.Pauta;
import com.desafio.votacao.domain.repository.PautaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PautaService - Testes Unitários")
class PautaServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @InjectMocks
    private PautaService pautaService;

    private CriarPautaRequest validRequest;
    private Pauta pautaSalva;

    @BeforeEach
    void setUp() {
        validRequest = CriarPautaRequest.builder()
                .titulo("Nova Pauta de Teste")
                .descricao("Descrição detalhada da pauta de teste")
                .build();

        pautaSalva = Pauta.builder()
                .id(UUID.randomUUID())
                .titulo(validRequest.getTitulo())
                .descricao(validRequest.getDescricao())
                .dataCriacao(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve criar uma nova pauta com sucesso")
    void deveCriarPautaComSucesso() {
        // Given
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pautaSalva);

        // When
        PautaResponse response = pautaService.criarPauta(validRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(pautaSalva.getId());
        assertThat(response.getTitulo()).isEqualTo(validRequest.getTitulo());
        assertThat(response.getDescricao()).isEqualTo(validRequest.getDescricao());
        assertThat(response.getDataCriacao()).isNotNull();

        verify(pautaRepository, times(1)).save(any(Pauta.class));
    }

    @Test
    @DisplayName("Deve buscar pauta por ID com sucesso")
    void deveBuscarPautaPorIdComSucesso() {
        // Given
        UUID pautaId = pautaSalva.getId();
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pautaSalva));

        // When
        PautaResponse response = pautaService.buscarPorId(pautaId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(pautaId);
        assertThat(response.getTitulo()).isEqualTo(pautaSalva.getTitulo());

        verify(pautaRepository, times(1)).findById(pautaId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pauta não for encontrada")
    void deveLancarExcecaoQuandoPautaNaoEncontrada() {
        // Given
        UUID pautaId = UUID.randomUUID();
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> pautaService.buscarPorId(pautaId))
                .isInstanceOf(PautaNaoEncontradaException.class)
                .hasMessageContaining(pautaId.toString());

        verify(pautaRepository, times(1)).findById(pautaId);
    }

    @Test
    @DisplayName("Deve listar todas as pautas")
    void deveListarTodasPautas() {
        // Given
        Pauta pauta1 = Pauta.builder()
                .id(UUID.randomUUID())
                .titulo("Pauta 1")
                .descricao("Descrição 1")
                .dataCriacao(LocalDateTime.now())
                .build();

        Pauta pauta2 = Pauta.builder()
                .id(UUID.randomUUID())
                .titulo("Pauta 2")
                .descricao("Descrição 2")
                .dataCriacao(LocalDateTime.now())
                .build();

        when(pautaRepository.findAll()).thenReturn(Arrays.asList(pauta1, pauta2));

        // When
        List<PautaResponse> response = pautaService.listarTodas();

        // Then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getTitulo()).isEqualTo("Pauta 1");
        assertThat(response.get(1).getTitulo()).isEqualTo("Pauta 2");

        verify(pautaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver pautas")
    void deveRetornarListaVaziaQuandoNaoHouverPautas() {
        // Given
        when(pautaRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<PautaResponse> response = pautaService.listarTodas();

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isEmpty();

        verify(pautaRepository, times(1)).findAll();
    }
}
