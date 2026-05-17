package com.finsightai.web.model.analysis;

import com.finsightai.web.model.analysis.enums.InsightType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "insights")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Insight {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private FinancialAnalysis analysis;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private InsightType type = InsightType.GENERAL;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (type == null) {
            type = InsightType.GENERAL;
        }
    }
}