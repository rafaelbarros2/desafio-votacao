package com.desafio.votacao.presentation.controller;

import com.desafio.votacao.application.dto.request.RegistrarVotoRequest;
import com.desafio.votacao.application.dto.response.VotoResponse;
import com.desafio.votacao.application.service.VotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/votos")
@RequiredArgsConstructor
@Tag(name = "Votos", description = "Registro de votos em sessões de votação")
public class VotoController {

    private final VotoService votoService;

    @PostMapping
    @Operation(summary = "Registrar voto", description = "Registra o voto de um associado em uma sessão de votação")
    public ResponseEntity<VotoResponse> registrarVoto(@Valid @RequestBody RegistrarVotoRequest request) {
        VotoResponse response = votoService.registrarVoto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
