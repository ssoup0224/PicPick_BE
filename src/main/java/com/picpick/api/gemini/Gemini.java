package com.picpick.api.gemini;

import com.picpick.scan.Scan;
import com.picpick.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "gemini")
public class Gemini {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "naver_image")
    private String naverImage;

    @Column(name = "naver_brand")
    private String naverBrand;

    @Column(name = "scan_name")
    private String scanName;

    @Column(name = "category")
    private String category;

    @Column(name = "pick_score")
    private Double pickScore;

    @Column(name = "reliability_score")
    private Double reliabilityScore;

    @Column(name = "scan_price")
    private Double scanPrice;

    @Column(name = "naver_price")
    private Double naverPrice;

    @Column(name = "price_diff")
    private Double priceDiff; // abs value; percent

    @Column(name = "is_cheaper")
    private Boolean isCheaper;

    @Column(name = "ai_unit_price")
    private String aiUnitPrice;

    @ElementCollection
    @CollectionTable(name = "gemini_indicators", joinColumns = @JoinColumn(name = "gemini_id"))
    private List<Indicator> indexes;

    @Column(name = "quality_summary")
    private String qualitySummary;

    @Column(name = "price_summary")
    private String priceSummary;

    @Column(name = "conclusion")
    private String conclusion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id")
    private Scan scan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Indicator {
        private String name;
        private String reason;
    }
}
