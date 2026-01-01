package com.picpick.services;

import com.picpick.api.naver.NaverProductDto;
import com.picpick.api.naver.NaverProductService;
import com.picpick.api.naver.NaverRequestVariableDto;
import com.picpick.dtos.ScanLogRequest;
import com.picpick.entities.*;
import com.picpick.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScanLogService {

    private final ScanLogRepository scanLogRepository;
    private final UserRepository userRepository;
    private final MartRepository martRepository;
    private final MartItemRepository martItemRepository;
    private final OnlineItemRepository onlineItemRepository;
    private final NaverProductService naverProductService;

    @Transactional
    public ScanLog saveScanLog(ScanLogRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with id: " + request.getUserId()));

        // existing logic to resolve mart...
        Mart mart = user.getCurrentMart();
        if (mart == null) {
            throw new IllegalArgumentException(
                    "User is not currently associated with any mart. Please update user location first.");
        }

        // 1) Ensure MartItem exists
        ensureMartItemExists(mart, request);

        // 2) Ensure OnlineItem with cheapest Naver price exists
        ensureOnlineItemFromNaver(request.getProductName());

        ScanLog scanLog = ScanLog.builder()
                .name(request.getProductName())
                .price(request.getPrice())
                .description(request.getDescription())
                .user(user)
                .mart(mart)
                .build();

        return scanLogRepository.save(scanLog);
    }

    @Transactional(readOnly = true)
    public List<ScanLog> getAllScanLogs() {
        return scanLogRepository.findAll();
    }

    private void ensureMartItemExists(Mart mart, ScanLogRequest request) {
        String itemName = request.getProductName();

        martItemRepository.findByMartIdAndItemName(mart.getId(), itemName)
                .orElseGet(() -> {
                    MartItem newItem = MartItem.builder()
                            .itemName(itemName)
                            .itemPrice(request.getPrice())
                            .startDate(LocalDate.now())    // or null / default
                            .endDate(LocalDate.now())      // or some default
                            .discountPercentage(null)      // not on sale
                            .mart(mart)
                            .build();

                    return martItemRepository.save(newItem);
                });
    }


    private void ensureOnlineItemFromNaver(String productName) {
        // If already cached, do nothing
        if (onlineItemRepository.findByItemName(productName).isPresent()) {
            return;
        }

        // Build Naver request: sort by price ascending, 1 result
        NaverRequestVariableDto var = NaverRequestVariableDto.builder()
                .query(productName)
                .display(1)      // only cheapest
                .start(1)
                .sort("asc")
                .build();

        List<NaverProductDto> results = naverProductService.naverShopSearchAPI(var);

        if (results.isEmpty()) {
            return;
        }

        NaverProductDto cheapest = results.get(0);

        OnlineItem item = OnlineItem.builder()
                .naverProductId(cheapest.getProductId())
                .itemName(productName)
                .itemPrice(cheapest.getLprice())
                .build();

        onlineItemRepository.save(item);
    }


}
