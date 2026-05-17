package com.finsightai.web.model.analysis;

import com.finsightai.web.model.User;
import com.finsightai.web.model.analysis.enums.AnalysisStatus;
import com.finsightai.web.model.analysis.enums.FinancialState;
import com.finsightai.web.model.enums.BankType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "financial_analyses")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FinancialAnalysis {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BankType bank;

    @Column(nullable = false, length = 7)
    private String period;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AnalysisStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "financial_state", length = 50)
    private FinancialState financialState;

    @Builder.Default
    @Column(name = "total_income", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_expense", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalExpense = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(columnDefinition = "text")
    private String message;

    @OneToMany(
            mappedBy = "analysis",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<CategoryAnalytics> categoryAnalytics = new ArrayList<>();

    @OneToMany(
            mappedBy = "analysis",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Insight> insights = new ArrayList<>();

    @OneToMany(
            mappedBy = "analysis",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Recommendation> recommendations = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addCategoryAnalytics(CategoryAnalytics category) {
        category.setAnalysis(this);
        this.categoryAnalytics.add(category);
    }

    public void addInsight(Insight insight) {
        insight.setAnalysis(this);
        this.insights.add(insight);
    }

    public void addRecommendation(Recommendation recommendation) {
        recommendation.setAnalysis(this);
        this.recommendations.add(recommendation);
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (totalIncome == null) {
            totalIncome = BigDecimal.ZERO;
        }

        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }

        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        if (totalIncome == null) {
            totalIncome = BigDecimal.ZERO;
        }

        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }

        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
    }
}