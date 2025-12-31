package com.picpick.repositories;

import com.picpick.entities.ScanLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ScanLogRepository extends JpaRepository<ScanLog, Long> {
        /**
         * 특정 유저의 모든 스캔 기록 (최신순)
         */
        @Query("SELECT s FROM ScanLog s WHERE s.user.id = :userId ORDER BY s.scannedAt DESC")
        List<ScanLog> findByUserId(@Param("userId") Long userId);

        /**
         * 특정 유저의 최근 N개 스캔
         */
        @Query("SELECT s FROM ScanLog s WHERE s.user.id = :userId ORDER BY s.scannedAt DESC")
        List<ScanLog> findRecentScans(@Param("userId") Long userId, Pageable pageable);

        /**
         * 특정 마트에서의 모든 스캔
         */
        List<ScanLog> findByMartId(Long martId);

        /**
         * 특정 유저가 방문한 마트 목록 (중복 제거)
         */
        @Query(value = "SELECT DISTINCT s.mart_id FROM scan_log s WHERE s.user_id = :userId " +
                        "ORDER BY s.scanned_at DESC", nativeQuery = true)
        List<Long> findDistinctMartIdsByUserId(@Param("userId") Long userId);

        /**
         * 특정 유저가 스캔한 상품 목록 (중복 제거)
         */
        @Query(value = "SELECT DISTINCT s.name FROM scan_log s WHERE s.user_id = :userId " +
                        "ORDER BY s.scanned_at DESC", nativeQuery = true)
        List<String> findDistinctProductNamesByUserId(@Param("userId") Long userId);
}
