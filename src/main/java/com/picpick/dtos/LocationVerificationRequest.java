package com.picpick.dtos;

import lombok.Data;

@Data
public class LocationVerificationRequest {
    private String uuid;
    private Double latitude;
    private Double longitude;
}
