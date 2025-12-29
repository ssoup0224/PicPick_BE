package com.picpick.naver;

import lombok.Getter;
import org.json.JSONObject;

@Getter
public class NaverProductDto {

    private String title;
    private String link;
    private String image;
    private Integer lprice;

    public NaverProductDto(JSONObject itemJson) {
        this.title = itemJson.getString("title");
        this.link = itemJson.getString("link");
        this.image = itemJson.getString("image");
        this.lprice = itemJson.getInt("lprice");
    }
}