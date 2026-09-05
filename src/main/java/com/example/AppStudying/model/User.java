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

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @JsonIgnore
    @Column(name = "deletion_token")
    private String deletionToken;

    @JsonIgnore
    @Column(name = "deletion_token_expiry")
    private LocalDateTime deletionTokenExpiry;

    // E-mail ainda não confirmado: enquanto o token não é validado, "email"
    // continua sendo o valor antigo — troca de e-mail nunca é imediata.
    @JsonIgnore
    @Column(name = "pending_email")
    private String pendingEmail;

    @JsonIgnore
    @Column(name = "email_change_token")
    private String emailChangeToken;

    @JsonIgnore
    @Column(name = "email_change_token_expiry")
    private LocalDateTime emailChangeTokenExpiry;

}
