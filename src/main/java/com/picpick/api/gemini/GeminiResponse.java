package com.picpick.api.gemini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiResponse {
    private String naverImage;
    private String naverBrand;
    private String scanName;
    private String category;
    private Double pickScore;
    private Double reliabilityScore;
    private Double scanPrice;
    private Double naverPrice;
    private Double priceDiff;
    private Boolean isCheaper;
    private String aiUnitPrice;
    private List<IndicatorDto> indexes;
    private String qualitySummary;
    private String priceSummary;
    private String conclusion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndicatorDto {
        private String name;
        private String reason;
    }
}
