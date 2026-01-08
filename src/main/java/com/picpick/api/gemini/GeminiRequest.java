package com.picpick.api.gemini;

import lombok.Data;

@Data
public class GeminiRequest {
    private String scanName;
    private Integer scanPrice;
    private Integer naverPrice;
    private String aiUnitPrice;
}
