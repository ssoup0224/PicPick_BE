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
import java.util.Optional;
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
        User user = userRepository.findById(userId)
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
                    Optional<Scan> existingScan = scanRepository
                            .findFirstByScanNameOrderByScannedAtDesc(item.getScanName());

                    if (existingScan.isPresent()) {
                        Scan es = existingScan.get();
                        log.info("Reusing existing Naver data for: {}", item.getScanName());
                        scan.setNaverProductId(es.getNaverProductId());
                        scan.setNaverBrand(es.getNaverBrand());
                        scan.setNaverMaker(es.getNaverMaker());
                        scan.setNaverName(es.getNaverName());
                        scan.setNaverPrice(es.getNaverPrice());
                        scan.setNaverImage(es.getNaverImage());
                        scan.setAiUnitPrice(es.getAiUnitPrice());
                    } else {
                        Naver naver = naverService.searchCheapest(item.getScanName());
                        if (naver != null) {
                            scan.setNaverProductId(naver.getProductId());
                            scan.setNaverBrand(naver.getBrand());
                            scan.setNaverMaker(naver.getMaker());
                            scan.setNaverName(naver.getTitle().replaceAll("<[^>]*>", ""));
                            scan.setNaverPrice(naver.getLprice());
                            scan.setNaverImage(naver.getImage());
                        }

                        // Gemini Unit Price for new item
                        UnitPriceResponse unitPriceResponse = geminiService
                                .getUnitPrice(new UnitPriceRequest(item.getScanName(), item.getScanPrice()));
                        scan.setAiUnitPrice(unitPriceResponse.getAiUnitPrice());
                    }

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

    @Transactional
    public List<ScanResponse> getScans(Long userId) {
        List<Scan> scans = scanRepository.findAllByUser_Id(userId);

        // Update isShown to true when fetched
        scans.forEach(scan -> {
            log.info("Scan retrieved: {}, naverImage: {}", scan.getScanName(), scan.getNaverImage());
            scan.setIsShown(true);
        });

        // Synchronously try to reuse existing analysis for scans that don't have one
        List<Scan> scansToAnalyze = scans.stream()
                .filter(scan -> scan.getGemini() == null)
                .filter(scan -> geminiService.findAndCloneAnalysis(scan, userId).isEmpty())
                .collect(Collectors.toList());

        // Background analysis only for scans that still need it
        if (!scansToAnalyze.isEmpty()) {
            geminiService.analyzeScansBatch(scansToAnalyze, userId);
        }

        return scans.stream()
                .map(scanMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void hideScans(Long userId) {
        List<Scan> scans = scanRepository.findAllByUser_Id(userId);
        scans.forEach(scan -> scan.setIsShown(false));
        log.info("Reset isShown to false for user ID: {}", userId);
    }

    public void deleteScannedItem(Long scanId) {
        scanRepository.deleteById(scanId);
    }
}