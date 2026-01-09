package com.picpick.api.gemini;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/gemini")
public class GeminiController {
    private final GeminiService geminiService;

    @GetMapping("/{scanId}")
    public ResponseEntity<?> getAnalyzedProductByScanId(
            @PathVariable Long scanId) {
        return geminiService.getAnalyzedProductByScanId(scanId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(Map.of(
                                "status", "pending",
                                "message", "분석 대기 중입니다. 잠시 후 다시 시도해주세요.",
                                "scanId", scanId)));
    }
}
