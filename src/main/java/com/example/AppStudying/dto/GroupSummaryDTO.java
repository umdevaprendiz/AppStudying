package com.example.AppStudying.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupSummaryDTO {
    private Long id;
    private String name;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private long memberCount;
    private long studyingCount;
}
