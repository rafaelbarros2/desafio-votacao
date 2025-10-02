package com.desafio.votacao.domain.exception;

import java.util.UUID;

public class SessaoFechadaException extends RuntimeException {

    public SessaoFechadaException(UUID sessaoId) {
        super("A sessão de votação " + sessaoId + " está fechada");
    }

    public SessaoFechadaException(String message) {
        super(message);
    }
}
