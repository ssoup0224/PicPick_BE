package com.picpick.scan;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ScanDto {
    private Long id;
    private String scanName;
    private Integer scanPrice;
    private LocalDateTime scannedAt;
    private String naverProductId;
    private String naverBrand;
    private String naverMaker;
    private String naverName;
    private Integer naverPrice;
    private String naverImage;
    private String aiUnitPrice;
}
