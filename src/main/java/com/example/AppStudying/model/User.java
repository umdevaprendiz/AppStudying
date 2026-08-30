package com.example.AppStudying.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "cpf", unique = true)
    private String cpf;

    @Column(name = "name")
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password")
    private String password;

    // Boolean (wrapper), não boolean primitivo: o Lombok @AllArgsConstructor
    // vira o creator que o Jackson usa pra desserializar, e um campo ausente
    // no JSON passa null pra esse construtor — inofensivo pros outros campos
    // (todos String/Long), mas quebraria num boolean primitivo.
    //
    // READ_ONLY (não WRITE_ONLY): o frontend precisa saber se a conta já foi
    // verificada, mas o cliente nunca pode setar isso direto no cadastro —
    // sempre começa false em UserService.registerUser, independente do que
    // vier no payload.
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @JsonIgnore
    @Column(name = "verification_token")
    private String verificationToken;

    @JsonIgnore
    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

}
