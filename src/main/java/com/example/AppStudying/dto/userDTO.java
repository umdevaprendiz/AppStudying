package com.example.AppStudying.dto;

import com.example.AppStudying.model.Matter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class userDTO {
    private String name;
    private LocalDateTime horaEstudo;
    private String email;
    private Matter matter;
}
