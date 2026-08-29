package com.example.AppStudying.enums;

public enum RequestStatus {

    PENDENTE("Pendente"),

    ACEITA("Aceita"),

    RECUSADA("Recusada");

    private final String descricao;

    RequestStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
