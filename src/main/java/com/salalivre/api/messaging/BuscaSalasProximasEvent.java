package com.salalivre.api.messaging;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuscaSalasProximasEvent {

    private String tipo = "BUSCA_SALAS_PROXIMAS";
    private String cepOrigem;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private Integer quantidadeResultados;

    public BuscaSalasProximasEvent() {
    }

    public BuscaSalasProximasEvent(String cepOrigem, LocalDate data, LocalTime horaInicio,
                                   LocalTime horaFim, Integer quantidadeResultados) {
        this.cepOrigem = cepOrigem;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.quantidadeResultados = quantidadeResultados;
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCepOrigem() { return cepOrigem; }
    public void setCepOrigem(String cepOrigem) { this.cepOrigem = cepOrigem; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    public Integer getQuantidadeResultados() { return quantidadeResultados; }
    public void setQuantidadeResultados(Integer quantidadeResultados) { this.quantidadeResultados = quantidadeResultados; }
}
