package com.picpick.dtos;

import com.picpick.entities.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String uuid;
    private Role role; // USER / MART
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private Integer totalScans;
    private Double currentLongitude;
    private Double currentLatitude;
}
