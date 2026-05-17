package com.finsightai.web.model.analysis;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "category_analytics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAnalytics {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private FinancialAnalysis analysis;

    @Column(nullable = false, length = 255)
    private String category;

    @Builder.Default
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percent = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "operations_count", nullable = false)
    private Integer operationsCount = 0;

    @Builder.Default
    @Column(name = "is_top", nullable = false)
    private Boolean isTop = false;

    @Column(name = "top_rank")
    private Integer topRank;

    @PrePersist
    protected void onCreate() {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        if (percent == null) {
            percent = BigDecimal.ZERO;
        }

        if (operationsCount == null) {
            operationsCount = 0;
        }

        if (isTop == null) {
            isTop = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        if (percent == null) {
            percent = BigDecimal.ZERO;
        }

        if (operationsCount == null) {
            operationsCount = 0;
        }

        if (isTop == null) {
            isTop = false;
        }
    }
}