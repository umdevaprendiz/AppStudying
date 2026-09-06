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
public class GroupMemberPresenceDTO {
    private Long userId;
    private String userName;
    private boolean studying;
    private String matterName;
    private String topicName;
    private LocalDateTime sessionStartedAt;
}
