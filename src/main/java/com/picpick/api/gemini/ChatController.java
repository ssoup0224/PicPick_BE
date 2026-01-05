package com.picpick.api.gemini;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gemini")
@RequiredArgsConstructor
public class ChatController {
    private final AnalysisService analysisService;

    @PostMapping("/analyze")
    public AnalysisAIResponse analyzeProduct(@RequestBody AnalysisRequest request) {
        return analysisService.generateReport(request);
    }
}
