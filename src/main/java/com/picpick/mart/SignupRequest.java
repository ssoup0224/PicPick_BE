package com.picpick.mart;

import lombok.Data;

import java.math.BigInteger;

@Data
public class SignupRequest {
    private String name;
    private String address;
    private BigInteger registrationNumber;
}
