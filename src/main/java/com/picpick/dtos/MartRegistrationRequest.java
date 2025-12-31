package com.picpick.dtos;

import lombok.Data;

@Data
public class MartRegistrationRequest {
    private String name;
    private String address;
    private String registrationNumber;
}
