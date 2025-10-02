package com.desafio.votacao.domain.exception;

public class CpfInvalidoException extends RuntimeException {

    public CpfInvalidoException(String cpf) {
        super("CPF inválido ou não autorizado a votar: " + maskCpf(cpf));
    }

    public CpfInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }

    private static String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) {
            return "***";
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }
}
