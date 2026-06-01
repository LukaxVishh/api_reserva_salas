// src/main/java/com/salalivre/api/service/DistanciaService.java
package com.salalivre.api.service;

import com.salalivre.api.model.Sala;
import com.salalivre.api.model.EnderecoViaCep;
import org.springframework.stereotype.Service;

@Service
public class DistanciaService {

    public static final String MUITO_PROXIMA = "MUITO_PROXIMA";
    public static final String PROXIMA = "PROXIMA";
    public static final String MEDIA = "MEDIA";
    public static final String DISTANTE = "DISTANTE";

    public double calcularDistanciaAproximadaKm(EnderecoViaCep origem, Sala sala) {
        if (origem == null || sala == null) return Double.MAX_VALUE;
        String origemCidade = safe(origem.getLocalidade());
        String origemBairro = safe(origem.getBairro());
        String origemUf = safe(origem.getUf());

        String salaCidade = safe(sala.getCidade());
        String salaBairro = safe(sala.getBairro());
        String salaUf = safe(sala.getUf());

        if (origemCidade.equalsIgnoreCase(salaCidade) && origemBairro.equalsIgnoreCase(salaBairro)) {
            return 2.0;
        } else if (origemCidade.equalsIgnoreCase(salaCidade)) {
            return 8.0;
        } else if (origemUf.equalsIgnoreCase(salaUf)) {
            return 80.0;
        } else {
            return 500.0;
        }
    }

    public String classificarProximidade(EnderecoViaCep origem, Sala sala) {
        if (origem == null || sala == null) return DISTANTE;
        String origemCidade = safe(origem.getLocalidade());
        String origemBairro = safe(origem.getBairro());
        String origemUf = safe(origem.getUf());

        String salaCidade = safe(sala.getCidade());
        String salaBairro = safe(sala.getBairro());
        String salaUf = safe(sala.getUf());

        if (origemCidade.equalsIgnoreCase(salaCidade) && origemBairro.equalsIgnoreCase(salaBairro)) {
            return MUITO_PROXIMA;
        } else if (origemCidade.equalsIgnoreCase(salaCidade)) {
            return PROXIMA;
        } else if (origemUf.equalsIgnoreCase(salaUf)) {
            return MEDIA;
        } else {
            return DISTANTE;
        }
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
}