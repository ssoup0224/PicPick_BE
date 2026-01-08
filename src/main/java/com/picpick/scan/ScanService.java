package com.picpick.scan;

import com.picpick.api.gemini.*;
import com.picpick.api.naver.Naver;
import com.picpick.api.naver.NaverService;
import com.picpick.user.User;
import com.picpick.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ScanService {
    private final ScanRepository scanRepository;
    private final UserRepository userRepository;
    private final ScanMapper scanMapper;
    private final NaverService naverService;
    private final GeminiService geminiService;

    @Transactional
    public CompletableFuture<List<ScanResponse>> processScans(ScanBatchRequest batchRequest) {
        Long userId = batchRequest.getUserId();

        log.info("Processing scan log for user ID: {}", userId);
        User user = userRepository.findWithMartById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Update user's total scan count
        int currentScans = user.getTotalScans() != null ? user.getTotalScans() : 0;
        user.setTotalScans(currentScans + batchRequest.getItems().size());

        List<Scan> scans = batchRequest.getItems().stream()
                .map(item -> {
                    Scan scan = scanMapper.toEntity(item);
                    scan.setScannedAt(LocalDateTime.now());
                    scan.setUser(user);
                    scan.setMart(user.getMart());

                    // Naver Shopping data
                    SearchRefineResponse refinedResponse = geminiService
                            .refineSearchQuery(new SearchRefineRequest(item.getScanName()));
                    String refinedQuery = refinedResponse.getRefinedQuery();

                    Naver naver = naverService.searchCheapest(refinedQuery);
                    if (naver != null) {
                        scan.setNaverProductId(naver.getProductId());
                        scan.setNaverBrand(naver.getBrand());
                        scan.setNaverMaker(naver.getMaker());
                        scan.setNaverName(naver.getTitle().replaceAll("<[^>]*>", ""));
                        scan.setNaverPrice(naver.getLprice());
                        scan.setNaverImage(naver.getImage());
                    }

                    // Gemini Unit Price
                    UnitPriceResponse unitPriceResponse = geminiService
                            .getUnitPrice(new UnitPriceRequest(refinedQuery, item.getScanPrice()));
                    scan.setAiUnitPrice(unitPriceResponse.getAiUnitPrice());

                    return scan;
                })
                .collect(Collectors.toList());

        List<Scan> savedScans = scanRepository.saveAll(scans);

        userRepository.save(user); // Ensure user count update is persisted
        log.info("Saved {} scans and updated total count for user {}", scans.size(), userId);

        List<ScanResponse> responses = savedScans.stream()
                .map(scanMapper::toResponse)
                .collect(Collectors.toList());

        return CompletableFuture.completedFuture(responses);
    }

    public List<ScanResponse> getScans(Long userId) {
        List<Scan> scans = scanRepository.findAllByUser_Id(userId);

        geminiService.analyzeScansBatch(scans, userId);

        return scans.stream()
                .map(scanMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteScannedItem(Long scanId) {
        scanRepository.deleteById(scanId);
    }
}
