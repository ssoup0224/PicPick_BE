package com.picpick.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDto {
    private Long id;
    private String uuid;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private Integer totalScans;
    private Double longitude;
    private Double latitude;
}
