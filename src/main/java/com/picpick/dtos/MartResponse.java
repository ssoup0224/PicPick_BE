package com.picpick.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MartResponse {
    private Long id;
    private String name;
    private String address;
    private Double longitude;
    private Double latitude;
    private String documentFile;
    private LocalDateTime createdAt;
    private List<MartItemResponse> items;
}
