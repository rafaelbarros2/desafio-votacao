package com.desafio.votacao.domain.exception;

import java.util.UUID;

public class SessaoVotacaoNaoEncontradaException extends RuntimeException {

    public SessaoVotacaoNaoEncontradaException(UUID id) {
        super("Sessão de votação não encontrada com ID: " + id);
    }

    public SessaoVotacaoNaoEncontradaException(String message) {
        super(message);
    }
}
