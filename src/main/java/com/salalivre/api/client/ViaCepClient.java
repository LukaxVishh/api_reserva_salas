package com.salalivre.api.client;

import com.salalivre.api.exception.CepInvalidoException;
import com.salalivre.api.model.EnderecoViaCep;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ViaCepClient {

    private static final String URL_VIACEP = "https://viacep.com.br/ws/%s/json/";

    private final RestTemplate restTemplate;

    public ViaCepClient() {
        this.restTemplate = new RestTemplate();
    }

    public EnderecoViaCep buscarEnderecoPorCep(String cep) {
        String url = String.format(URL_VIACEP, cep);

        try {
            EnderecoViaCep endereco = restTemplate.getForObject(url, EnderecoViaCep.class);

            if (endereco == null) {
                throw new CepInvalidoException("CEP inválido ou não encontrado.");
            }

            return endereco;

        } catch (RestClientException ex) {
            throw new CepInvalidoException("Erro ao consultar o ViaCEP.");
        }
    }
}