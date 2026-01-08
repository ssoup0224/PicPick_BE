package com.picpick.api.naver;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Naver {
    private String title;
    private String link;
    private String image;

    @JsonProperty("lprice")
    private Integer lprice; // lowest price

    @JsonProperty("hprice")
    private Integer hprice; // highest price

    private String mallName;
    private String productId;
    private String productType;
    private String brand;
    private String maker;
    private String category1;
    private String category2;
    private String category3;
    private String category4;
}
