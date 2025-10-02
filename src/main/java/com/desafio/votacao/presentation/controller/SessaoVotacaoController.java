package com.desafio.votacao.presentation.controller;

import com.desafio.votacao.application.dto.request.AbrirSessaoRequest;
import com.desafio.votacao.application.dto.response.ResultadoVotacaoResponse;
import com.desafio.votacao.application.dto.response.SessaoVotacaoResponse;
import com.desafio.votacao.application.service.SessaoVotacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessoes")
@RequiredArgsConstructor
@Tag(name = "Sessões de Votação", description = "Gerenciamento de sessões de votação")
public class SessaoVotacaoController {

    private final SessaoVotacaoService sessaoService;

    @PostMapping
    @Operation(summary = "Abrir sessão de votação", description = "Abre uma nova sessão de votação para uma pauta")
    public ResponseEntity<SessaoVotacaoResponse> abrirSessao(@Valid @RequestBody AbrirSessaoRequest request) {
        SessaoVotacaoResponse response = sessaoService.abrirSessao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar sessão por ID", description = "Retorna os detalhes de uma sessão de votação")
    public ResponseEntity<SessaoVotacaoResponse> buscarSessao(@PathVariable UUID id) {
        SessaoVotacaoResponse response = sessaoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/resultado")
    @Operation(summary = "Obter resultado da votação", description = "Retorna o resultado contabilizado de uma sessão de votação")
    public ResponseEntity<ResultadoVotacaoResponse> obterResultado(@PathVariable UUID id) {
        ResultadoVotacaoResponse response = sessaoService.obterResultado(id);
        return ResponseEntity.ok(response);
    }
}
