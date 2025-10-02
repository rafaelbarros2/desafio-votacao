package com.desafio.votacao.infrastructure.client;

import com.desafio.votacao.domain.exception.CpfInvalidoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Slf4j
public class CpfValidationClient {

    private final Random random = new Random();

    /**
     * Cliente fake para validação de CPF (Bônus 1)
     * Retorna aleatoriamente se o CPF pode votar ou não
     *
     * @param cpf CPF do associado
     * @return true se pode votar, false caso contrário
     * @throws CpfInvalidoException se o CPF não pode votar
     */
    public boolean validarCpf(String cpf) {
        log.info("Validando CPF: {}", maskCpf(cpf));

        // Simula validação externa com retorno aleatório
        boolean canVote = random.nextBoolean();

        if (!canVote) {
            log.warn("CPF não autorizado a votar: {}", maskCpf(cpf));
            throw new CpfInvalidoException(cpf);
        }

        log.info("CPF validado com sucesso: {}", maskCpf(cpf));
        return true;
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) {
            return "***";
        }
        String cleaned = cpf.replaceAll("[^0-9]", "");
        if (cleaned.length() < 11) {
            return "***";
        }
        return cleaned.substring(0, 3) + ".***.***-" + cleaned.substring(9);
    }
}
