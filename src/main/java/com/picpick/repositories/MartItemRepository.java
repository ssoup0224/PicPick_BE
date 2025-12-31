package com.picpick.repositories;

import com.picpick.entities.MartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MartItemRepository extends JpaRepository<MartItem, Long> {
        /**
         * 특정 마트의 모든 상품
         */
        List<MartItem> findByMartId(Long martId);

        /**
         * 특정 마트에서 특정 상품명 있는지 확인
         */
        Optional<MartItem> findByMartIdAndItemName(Long martId, String itemName);

        /**
         * 특정 마트에서 진행 중인 행사 상품들 (현재 날짜 기준)
         */
        @Query("SELECT mi FROM MartItem mi WHERE mi.mart.id = :martId " +
                        "AND mi.startDate <= :currentDate AND mi.endDate >= :currentDate ")
        List<MartItem> findActivePromotionsForMart(
                        @Param("martId") Long martId,
                        @Param("currentDate") LocalDate currentDate);
}
