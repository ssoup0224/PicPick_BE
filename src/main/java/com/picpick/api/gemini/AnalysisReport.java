package com.picpick.api.gemini;

import com.picpick.entities.ScanLog;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "analysis_report")
public class AnalysisReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "chosen_category")
    private String chosenCategory;

    @Column(name = "pick_score")
    private Double pickScore;

    @Column(name = "credibility_score")
    private Double credibilityScore;

    @Column(name = "mart_price")
    private Integer martPrice;

    @Column(name = "online_price")
    private Integer onlinePrice;

    @Column(name = "price_difference_percent")
    private Double priceDifferencePercent;

    @Column(name = "index_one")
    private String indexOne;

    @Column(name = "index_two")
    private String indexTwo;

    @Column(name = "index_three")
    private String indexThree;

    @Column(name = "index_four")
    private String indexFour;

    @Column(name = "index_five")
    private String indexFive;

    @Column(name = "quality_info")
    private String qualityInfo;

    @Column(name = "price_info")
    private String priceInfo;

    @Column(name = "conclusion_info")
    private String conclusionInfo;

    @OneToOne
    @JoinColumn(name = "scan_log_id")
    private ScanLog scanLog;
}
