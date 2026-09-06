package com.example.AppStudying.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// "study_groups" em vez de "group": GROUP e reservado no MySQL (GROUP BY)
// e quebraria qualquer query gerada pelo Hibernate se usado como nome de tabela.
// entity name "StudyGroup" pelo mesmo motivo do lado do JPQL: GROUP tambem e
// palavra reservada na gramatica do HQL/JPQL (usada em "GROUP BY").
@Entity(name = "StudyGroup")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "study_groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
