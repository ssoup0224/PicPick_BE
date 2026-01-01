package com.picpick.controllers;

import com.picpick.dtos.ScanLogRequest;
import com.picpick.dtos.ScanLogResponse;
import com.picpick.entities.ScanLog;
import com.picpick.mappers.ScanLogMapper;
import com.picpick.services.ScanLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scanlogs")
public class ScanLogController {

    private final ScanLogService scanLogService;
    private final ScanLogMapper scanLogMapper;

    @PostMapping
    public ResponseEntity<ScanLogResponse> createScanLog(@RequestBody ScanLogRequest request) {
        ScanLog savedLog = scanLogService.saveScanLog(request);
        return ResponseEntity.ok(scanLogMapper.toDto(savedLog));
    }

    @GetMapping
    public ResponseEntity<List<ScanLogResponse>> getScanLogs() {
        return ResponseEntity.ok(
                scanLogService.getAllScanLogs().stream()
                        .map(scanLogMapper::toDto)
                        .toList()
        );
    }
}

