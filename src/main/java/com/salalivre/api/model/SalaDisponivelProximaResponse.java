package com.salalivre.api.model;

public class SalaDisponivelProximaResponse {
    private Integer salaId;
    private String nome;
    private String bloco;
    private Integer capacidade;
    private Boolean temProjetor;
    private String cep;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String uf;
    private Double distanciaAproximadaKm;
    private String classificacaoProximidade;

    public SalaDisponivelProximaResponse() {}

    public SalaDisponivelProximaResponse(
            Integer salaId,
            String nome,
            String bloco,
            Integer capacidade,
            Boolean temProjetor,
            String cep,
            String logradouro,
            String bairro,
            String cidade,
            String uf,
            Double distanciaAproximadaKm,
            String classificacaoProximidade
    ) {
        this.salaId = salaId;
        this.nome = nome;
        this.bloco = bloco;
        this.capacidade = capacidade;
        this.temProjetor = temProjetor;
        this.cep = cep;
        this.logradouro = logradouro;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf;
        this.distanciaAproximadaKm = distanciaAproximadaKm;
        this.classificacaoProximidade = classificacaoProximidade;
    }

    public Integer getSalaId() { return salaId; }
    public void setSalaId(Integer salaId) { this.salaId = salaId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getBloco() { return bloco; }
    public void setBloco(String bloco) { this.bloco = bloco; }
    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }
    public Boolean getTemProjetor() { return temProjetor; }
    public void setTemProjetor(Boolean temProjetor) { this.temProjetor = temProjetor; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public Double getDistanciaAproximadaKm() { return distanciaAproximadaKm; }
    public void setDistanciaAproximadaKm(Double distanciaAproximadaKm) { this.distanciaAproximadaKm = distanciaAproximadaKm; }
    public String getClassificacaoProximidade() { return classificacaoProximidade; }
    public void setClassificacaoProximidade(String classificacaoProximidade) { this.classificacaoProximidade = classificacaoProximidade; }
}