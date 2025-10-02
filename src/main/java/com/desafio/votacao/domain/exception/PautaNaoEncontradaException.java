package com.desafio.votacao.domain.exception;

import java.util.UUID;

public class PautaNaoEncontradaException extends RuntimeException {

    public PautaNaoEncontradaException(UUID id) {
        super("Pauta não encontrada com ID: " + id);
    }

    public PautaNaoEncontradaException(String message) {
        super(message);
    }
}
