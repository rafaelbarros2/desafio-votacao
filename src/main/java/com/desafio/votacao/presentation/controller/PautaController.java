package com.desafio.votacao.presentation.controller;

import com.desafio.votacao.application.dto.request.CriarPautaRequest;
import com.desafio.votacao.application.dto.response.PautaResponse;
import com.desafio.votacao.application.service.PautaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pautas")
@RequiredArgsConstructor
@Tag(name = "Pautas", description = "Gerenciamento de pautas de votação")
public class PautaController {

    private final PautaService pautaService;

    @PostMapping
    @Operation(summary = "Criar uma nova pauta", description = "Cria uma nova pauta para votação")
    public ResponseEntity<PautaResponse> criarPauta(@Valid @RequestBody CriarPautaRequest request) {
        PautaResponse response = pautaService.criarPauta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pauta por ID", description = "Retorna os detalhes de uma pauta específica")
    public ResponseEntity<PautaResponse> buscarPauta(@PathVariable UUID id) {
        PautaResponse response = pautaService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas as pautas", description = "Retorna todas as pautas cadastradas")
    public ResponseEntity<List<PautaResponse>> listarPautas() {
        List<PautaResponse> response = pautaService.listarTodas();
        return ResponseEntity.ok(response);
    }
}
