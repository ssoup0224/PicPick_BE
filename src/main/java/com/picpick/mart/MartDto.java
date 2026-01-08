package com.picpick.mart;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;

@Data
@Builder
public class MartDto {
    private String name;
    private String address;
    private BigInteger registrationNumber;
    private Double latitude;
    private Double longitude;
}
