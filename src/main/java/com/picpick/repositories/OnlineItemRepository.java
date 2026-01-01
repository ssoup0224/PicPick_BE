package com.picpick.repositories;

import com.picpick.entities.OnlineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OnlineItemRepository extends JpaRepository<OnlineItem, Long> {
     // 상품명으로 조회
    Optional<OnlineItem> findByItemName(String itemName);

     // 네이버 상품 ID로 조회 (캐싱용)
    Optional<OnlineItem> findByNaverProductId(String naverProductId);
}
