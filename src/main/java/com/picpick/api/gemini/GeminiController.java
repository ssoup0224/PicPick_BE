package com.picpick.api.gemini;

import com.picpick.api.gemini.GeminiRequest;
import com.picpick.api.gemini.GeminiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/gemini")
public class GeminiController {
    private final GeminiService geminiService;

    @PostMapping("/analyze")
    public GeminiResponse analyzeProduct(@RequestBody GeminiRequest request) {
        return geminiService.analyzeProduct(request);
    }

    @GetMapping("/{scanId}")
    public ResponseEntity<GeminiResponse> getAnalyzedProductByScanId(
            @PathVariable Long scanId) {
        return geminiService.getAnalyzedProductByScanId(scanId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
