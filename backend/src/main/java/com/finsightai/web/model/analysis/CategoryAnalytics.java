package com.finsightai.web.model.analysis;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "category_analytics")
@Data
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

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percent;

    @Column(name = "operations_count", nullable = false)
    private Integer operationsCount;

    @Column(name = "is_top", nullable = false)
    private Boolean isTop;

    @Column(name = "top_rank")
    private Integer topRank;
}