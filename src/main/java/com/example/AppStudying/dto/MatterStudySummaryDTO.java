package com.example.AppStudying.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatterStudySummaryDTO {
    private String matterName;
    private long totalMinutes;
    private long totalSessions;
}
