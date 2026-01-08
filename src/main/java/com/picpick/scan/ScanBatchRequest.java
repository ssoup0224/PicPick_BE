package com.picpick.scan;

import lombok.Data;

import java.util.List;

@Data
public class ScanBatchRequest {
    private Long userId;
    private List<ScanRequest> items;
}
