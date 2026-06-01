package com.salalivre.api.messaging;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservaCriadaEvent {

    private String tipo = "RESERVA_CRIADA";
    private Integer reservaId;
    private Integer salaId;
    private String nomeResponsavel;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;

    public ReservaCriadaEvent() {
    }

    public ReservaCriadaEvent(Integer reservaId, Integer salaId, String nomeResponsavel,
                              LocalDate data, LocalTime horaInicio, LocalTime horaFim) {
        this.reservaId = reservaId;
        this.salaId = salaId;
        this.nomeResponsavel = nomeResponsavel;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getReservaId() { return reservaId; }
    public void setReservaId(Integer reservaId) { this.reservaId = reservaId; }

    public Integer getSalaId() { return salaId; }
    public void setSalaId(Integer salaId) { this.salaId = salaId; }

    public String getNomeResponsavel() { return nomeResponsavel; }
    public void setNomeResponsavel(String nomeResponsavel) { this.nomeResponsavel = nomeResponsavel; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
}
