package com.artificial.SpringAIDemo.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DiscenteDTO {

    private Long idDiscente;
    private String nome;
    private String cognome;
    private String matricola;
    private Integer eta;
    private String cittaResidenza;

    public DiscenteDTO() {}

    public DiscenteDTO(String nome, String cognome, String matricola, Integer eta, String cittaResidenza,Long idDiscente) {
        this.nome = nome;
        this.cognome = cognome;
        this.matricola = matricola;
        this.eta = eta;
        this.cittaResidenza = cittaResidenza;
        this.idDiscente = idDiscente;

    }

    public Long getIdDiscente() {
        return idDiscente;
    }

    public void setIdDiscente(Long idDiscente) {
        this.idDiscente = idDiscente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getMatricola() {
        return matricola;
    }

    public void setMatricola(String matricola) {
        this.matricola = matricola;
    }

    public Integer getEta() {
        return eta;
    }

    public void setEta(Integer eta) {
        this.eta = eta;
    }

    public String getCittaResidenza() {
        return cittaResidenza;
    }

    public void setCittaResidenza(String cittaResidenza) {
        this.cittaResidenza = cittaResidenza;
    }
}
