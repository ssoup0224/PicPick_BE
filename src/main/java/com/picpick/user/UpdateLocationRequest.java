package com.picpick.user;

import lombok.Data;

@Data
public class UpdateLocationRequest {
    private Long userId;
    private Double latitude;
    private Double longitude;
}
