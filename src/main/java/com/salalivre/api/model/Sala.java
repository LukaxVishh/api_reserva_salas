package com.salalivre.api.model;

public class Sala {

    private Integer id;
    private String nome;
    private String bloco;
    private Integer capacidade;
    private Boolean temProjetor;
    private Boolean ativa;

    public Sala() {
    }

    public Sala(Integer id, String nome, String bloco, Integer capacidade, Boolean temProjetor, Boolean ativa) {
        this.id = id;
        this.nome = nome;
        this.bloco = bloco;
        this.capacidade = capacidade;
        this.temProjetor = temProjetor;
        this.ativa = ativa;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getBloco() {
        return bloco;
    }

    public void setBloco(String bloco) {
        this.bloco = bloco;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
    }

    public Boolean getTemProjetor() {
        return temProjetor;
    }

    public void setTemProjetor(Boolean temProjetor) {
        this.temProjetor = temProjetor;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }
}