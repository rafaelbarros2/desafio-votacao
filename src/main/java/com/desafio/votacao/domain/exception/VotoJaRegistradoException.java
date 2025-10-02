package com.desafio.votacao.domain.exception;

import java.util.UUID;

public class VotoJaRegistradoException extends RuntimeException {

    public VotoJaRegistradoException(String cpf, UUID sessaoId) {
        super("Associado com CPF " + maskCpf(cpf) + " já votou na sessão " + sessaoId);
    }

    public VotoJaRegistradoException(String message) {
        super(message);
    }

    private static String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) {
            return "***";
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }
}
