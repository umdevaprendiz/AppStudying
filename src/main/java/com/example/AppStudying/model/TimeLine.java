package com.example.AppStudying.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TimeLine {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
}
