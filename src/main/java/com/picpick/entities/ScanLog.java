package com.picpick.entities;

import com.picpick.api.gemini.AnalysisReport;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "scan_log")
public class ScanLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "description")
    private String description;

    @Column(name = "scanned_at", nullable = false, updatable = false)
    private LocalDateTime scannedAt;

    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne()
    @JoinColumn(name = "mart_id", nullable = false)
    private Mart mart;

    @OneToOne()
    @JoinColumn(name = "online_item_id")
    private OnlineItem onlineItem;

    @OneToOne(mappedBy = "scanLog", cascade = CascadeType.ALL)
    private AnalysisReport analysisReport;

    @PrePersist
    protected void onCreate() {
        if (this.scannedAt == null) {
            this.scannedAt = LocalDateTime.now();
        }
    }
}
