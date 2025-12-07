package com.vitalistech.sosrota.web.dto;

/**
 * DTO para retornar bairros sugeridos para posicionamento de ambulâncias.
 */
public class BairroSugeridoDTO {
    private Long id;
    private String nome;
    private String justificativa;
    private int ocorrenciasNoBairro;
    private double tempoMedioResposta;
    private int ambulanciasExistentes;
    private int bairrosAlcancaveis;

    public BairroSugeridoDTO() {
    }

    public BairroSugeridoDTO(Long id, String nome, String justificativa, 
                             int ocorrenciasNoBairro, double tempoMedioResposta, 
                             int ambulanciasExistentes, int bairrosAlcancaveis) {
        this.id = id;
        this.nome = nome;
        this.justificativa = justificativa;
        this.ocorrenciasNoBairro = ocorrenciasNoBairro;
        this.tempoMedioResposta = tempoMedioResposta;
        this.ambulanciasExistentes = ambulanciasExistentes;
        this.bairrosAlcancaveis = bairrosAlcancaveis;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public int getOcorrenciasNoBairro() {
        return ocorrenciasNoBairro;
    }

    public void setOcorrenciasNoBairro(int ocorrenciasNoBairro) {
        this.ocorrenciasNoBairro = ocorrenciasNoBairro;
    }

    public double getTempoMedioResposta() {
        return tempoMedioResposta;
    }

    public void setTempoMedioResposta(double tempoMedioResposta) {
        this.tempoMedioResposta = tempoMedioResposta;
    }

    public int getAmbulanciasExistentes() {
        return ambulanciasExistentes;
    }

    public void setAmbulanciasExistentes(int ambulanciasExistentes) {
        this.ambulanciasExistentes = ambulanciasExistentes;
    }

    public int getBairrosAlcancaveis() {
        return bairrosAlcancaveis;
    }

    public void setBairrosAlcancaveis(int bairrosAlcancaveis) {
        this.bairrosAlcancaveis = bairrosAlcancaveis;
    }
}

