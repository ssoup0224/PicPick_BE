package com.picpick.api.gemini;

import lombok.Data;
import java.util.List;

@Data
public class AnalysisAIResponse {
    private String productName;
    private String chosenCategory;
    private Double pickScore;
    private Double credibilityScore;
    private String pickPriceInfo;
    private Double priceDifferencePercent;
    private Boolean isCheaperThanOnline;
    private List<Indicator> indices;
    private String qualitySummary;
    private String priceSummary;
    private String conclusion;

    @Data
    public static class Indicator {
        private String name;
        private String reason;
    }
}
