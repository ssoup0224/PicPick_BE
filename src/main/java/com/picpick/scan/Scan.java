package com.picpick.scan;

import com.picpick.api.gemini.Gemini;
import com.picpick.mart.Mart;
import com.picpick.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "scans")
public class Scan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 스캔해서 받아오는 정보
    @Column(name = "scan_name", nullable = false)
    private String scanName;

    @Column(name = "scan_price", nullable = false)
    private Integer scanPrice;

    @Column(name = "scanned_at", updatable = false)
    private LocalDateTime scannedAt;

    // 네이버 API 통해 받아오는 정보
    @Column(name = "naver_product_id")
    private String naverProductId;

    @Column(name = "naver_brand")
    private String naverBrand;

    @Column(name = "naver_maker")
    private String naverMaker;

    @Column(name = "naver_name")
    private String naverName;

    @Column(name = "naver_price")
    private Integer naverPrice;

    @Column(name = "naver_image")
    private String naverImage;

    // Gemini 통해 받아오는 정보
    @Column(name = "ai_unit_price")
    private String aiUnitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mart_id")
    private Mart mart;

    @OneToOne(mappedBy = "scan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Gemini gemini;
}
