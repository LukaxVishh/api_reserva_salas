package com.salalivre.api.model;

import java.time.LocalDateTime;

public class EventoSistema {

    private Integer id;
    private String tipo;
    private String payload;
    private LocalDateTime criadoEm;

    public EventoSistema() {
    }

    public EventoSistema(Integer id, String tipo, String payload, LocalDateTime criadoEm) {
        this.id = id;
        this.tipo = tipo;
        this.payload = payload;
        this.criadoEm = criadoEm;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
