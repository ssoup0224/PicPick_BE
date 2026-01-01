package com.picpick.api.naver;

import lombok.Getter;
import org.json.JSONObject;

@Getter
public class NaverProductDto {

    private String productId;
    private String title;
    private String link;
    private String image;
    private Integer lprice;

    public NaverProductDto(JSONObject itemJson) {
        this.productId = itemJson.getString("productId");
        this.title = itemJson.getString("title");
        this.link = itemJson.getString("link");
        this.image = itemJson.getString("image");
        this.lprice = itemJson.getInt("lprice");
    }
}
