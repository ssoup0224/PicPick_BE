package com.picpick.scan;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/scan")
public class ScanController {
    private final ScanService scanService;

    @PostMapping()
    public CompletableFuture<ResponseEntity<List<ScanResponse>>> createScans(
            @RequestBody ScanBatchRequest request) {
        log.info("Received scan request for user: {}", request.getUserId());
        return scanService.processScans(request)
                .thenApply(responses -> ResponseEntity.status(HttpStatus.CREATED).body(responses));
    }

    @GetMapping()
    public ResponseEntity<List<ScanResponse>> getScanItems(@RequestParam Long userId) {
        return ResponseEntity.ok(scanService.getScans(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteScannedItem(@PathVariable Long id) {
        scanService.deleteScannedItem(id);
        return ResponseEntity.ok(Map.of("message", "상품 삭제되었습니다."));
    }
}
