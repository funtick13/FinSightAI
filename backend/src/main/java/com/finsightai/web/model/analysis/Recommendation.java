package com.finsightai.web.model.analysis;

import com.finsightai.web.model.analysis.enums.RecommendationType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recommendations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private FinancialAnalysis analysis;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private RecommendationType type = RecommendationType.GENERAL_ADVICE;

    @Column(length = 255)
    private String category;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(precision = 19, scale = 2)
    private BigDecimal value;

    @Column(precision = 5, scale = 2)
    private BigDecimal percent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (type == null) {
            type = RecommendationType.GENERAL_ADVICE;
        }
    }
}