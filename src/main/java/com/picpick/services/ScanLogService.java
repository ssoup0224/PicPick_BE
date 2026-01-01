package com.picpick.services;

import com.picpick.dtos.ScanLogRequest;
import com.picpick.entities.Mart;
import com.picpick.entities.ScanLog;
import com.picpick.entities.User;
import com.picpick.repositories.MartRepository;
import com.picpick.repositories.ScanLogRepository;
import com.picpick.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScanLogService {

        private final ScanLogRepository scanLogRepository;
        private final UserRepository userRepository;
        private final MartRepository martRepository;

        @Transactional
        public ScanLog saveScanLog(ScanLogRequest request) {
                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "User not found with id: " + request.getUserId()));

                // Use the mart already associated with the user, or find it based on user's
                // stored location
                if (user.getCurrentMart() == null) {
                        if (user.getCurrentLatitude() != null && user.getCurrentLongitude() != null) {
                                // Find nearest mart using DB location
                                List<Mart> nearbyMarts = martRepository.findNearestMart(
                                                user.getCurrentLatitude(),
                                                user.getCurrentLongitude(),
                                                PageRequest.of(0, 1));

                                if (!nearbyMarts.isEmpty()) {
                                        user.setCurrentMart(nearbyMarts.get(0));
                                }
                        }
                }

                // If user doesn't have a mart yet, try the martId from request as a last resort
                if (user.getCurrentMart() == null && request.getMartId() != null) {
                        Mart fallbackMart = martRepository.findById(request.getMartId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Mart not found with id: " + request.getMartId()));
                        user.setCurrentMart(fallbackMart);
                }

                Mart mart = user.getCurrentMart();
                if (mart == null) {
                        throw new IllegalArgumentException(
                                        "User is not currently associated with any mart. Please update user location first.");
                }

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
}
