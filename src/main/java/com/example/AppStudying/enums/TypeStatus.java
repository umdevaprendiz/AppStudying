package com.example.AppStudying.enums;


public enum TypeStatus {

    PENDENTE("Pendente"),

    EM_ANDAMENTO("Em andamento"),

    CONCLUIDO("Concluído");

    private final String descricao;

    TypeStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
