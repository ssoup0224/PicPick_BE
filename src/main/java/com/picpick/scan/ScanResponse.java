package com.picpick.scan;

import lombok.Data;

@Data
public class ScanResponse {
    private Long userId;
    private Long scanId;
    private String scanName;
    private Integer scanPrice;
    private Integer naverPrice;
    private String naverBrand;
    private String naverMaker;
    private String naverImage;
    private String aiUnitPrice;
    private Boolean isShown;
}
