package com.picpick.api.naver;

import lombok.Data;

import java.util.List;

@Data
public class NaverResponse {
    private String lastBuildDate;
    private Integer total;
    private Integer start;
    private Integer display;
    private List<Naver> items;
}
