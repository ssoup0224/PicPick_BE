package com.picpick.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanLogRequest {
    private String productName;
    private Integer price;
    private String description;
    private Long userId;
    private Long martId;
}
