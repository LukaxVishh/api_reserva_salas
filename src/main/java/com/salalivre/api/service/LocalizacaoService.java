package com.salalivre.api.service;

import com.salalivre.api.client.ViaCepClient;
import com.salalivre.api.exception.CepInvalidoException;
import com.salalivre.api.model.EnderecoViaCep;
import org.springframework.stereotype.Service;

@Service
public class LocalizacaoService {

    private final ViaCepClient viaCepClient;

    public LocalizacaoService(ViaCepClient viaCepClient) {
        this.viaCepClient = viaCepClient;
    }

    public EnderecoViaCep buscarEnderecoPorCep(String cep) {
        String cepNormalizado = normalizarCep(cep);

        EnderecoViaCep endereco = viaCepClient.buscarEnderecoPorCep(cepNormalizado);

        if (Boolean.TRUE.equals(endereco.getErro())) {
            throw new CepInvalidoException("CEP inválido ou não encontrado.");
        }

        endereco.setCep(cepNormalizado);

        return endereco;
    }

    public String normalizarCep(String cep) {
        if (cep == null || cep.trim().isEmpty()) {
            throw new CepInvalidoException("O CEP é obrigatório.");
        }

        String cepNormalizado = cep.replaceAll("\\D", "");

        if (!cepNormalizado.matches("\\d{8}")) {
            throw new CepInvalidoException("O CEP deve conter exatamente 8 números.");
        }

        return cepNormalizado;
    }
}