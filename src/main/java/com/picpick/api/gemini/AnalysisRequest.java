package com.picpick.api.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    private String productName;
    private Integer martPrice;
    private Integer onlinePrice;
    private Long scanLogId;
}
