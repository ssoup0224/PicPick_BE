package com.picpick.services;

import com.picpick.api.naver.NaverProductDto;
import com.picpick.api.naver.NaverProductService;
import com.picpick.api.naver.NaverRequestVariableDto;
import com.picpick.dtos.ScanLogRequest;
import com.picpick.entities.*;
import com.picpick.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanLogService {

    private final ScanLogRepository scanLogRepository;
    private final UserRepository userRepository;
    private final MartItemRepository martItemRepository;
    private final OnlineItemRepository onlineItemRepository;
    private final NaverProductService naverProductService;

    public ScanLog saveScanLog(ScanLogRequest request) {
        log.info("Processing scan log for user ID: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));

        Mart mart = user.getCurrentMart();

        // This is where your error is triggering
        if (mart == null) {
            log.error("Failed to create scan log: User {} has no current mart assigned.", user.getId());
            throw new IllegalStateException("User is not at a mart location.");
        }

        log.info("User is currently at mart: {}", mart.getName());

        // 1. Fetch Naver data outside of DB transaction
        NaverProductDto cheapestOnline = null;
        if (onlineItemRepository.findByItemName(request.getProductName()).isEmpty()) {
            cheapestOnline = fetchCheapestFromNaver(request.getProductName());
        }

        // 2. Perform DB updates in a separate transaction
        return executeDatabaseOperations(user, mart, request, cheapestOnline);
    }

    @Transactional
    protected ScanLog executeDatabaseOperations(User user, Mart mart, ScanLogRequest request,
            NaverProductDto cheapest) {
        ensureMartItemExists(mart, request);

        OnlineItem onlineItem = onlineItemRepository.findByItemName(request.getProductName())
                .orElse(null);

        if (cheapest != null) {
            onlineItem = OnlineItem.builder()
                    .naverProductId(cheapest.getProductId())
                    .itemBrand(cheapest.getProductBrand())
                    .itemName(request.getProductName())
                    .itemPrice(cheapest.getLowestPrice())
                    .build();
            onlineItem = onlineItemRepository.save(onlineItem);
        }

        ScanLog scanLog = ScanLog.builder()
                .name(request.getProductName())
                .price(request.getPrice())
                .description(request.getDescription())
                .user(user)
                .mart(mart)
                .onlineItem(onlineItem)
                .build();

        return scanLogRepository.save(scanLog);
    }

    private NaverProductDto fetchCheapestFromNaver(String productName) {
        NaverRequestVariableDto var = NaverRequestVariableDto.builder()
                .query(productName).display(1).start(1).sort("asc").build();
        List<NaverProductDto> results = naverProductService.naverShopSearchAPI(var);
        return results.isEmpty() ? null : results.get(0);
    }

    private void ensureMartItemExists(Mart mart, ScanLogRequest request) {
        martItemRepository.findByMartIdAndItemName(mart.getId(), request.getProductName())
                .orElseGet(() -> martItemRepository.save(MartItem.builder()
                        .itemName(request.getProductName())
                        .itemPrice(request.getPrice())
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now())
                        .mart(mart)
                        .build()));
    }

    @Transactional(readOnly = true)
    public List<ScanLog> getScanLogsByUserId(Long userId) {
        return scanLogRepository.findByUserId(userId);
    }
}