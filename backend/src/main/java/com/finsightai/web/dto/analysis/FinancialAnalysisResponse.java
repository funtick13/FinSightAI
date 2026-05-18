package com.finsightai.web.dto.analysis;

import com.finsightai.web.model.analysis.enums.AnalysisStatus;
import com.finsightai.web.model.analysis.enums.FinancialState;
import com.finsightai.web.model.enums.BankType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FinancialAnalysisResponse {

    private UUID id;

    private UUID requestId;

    private BankType bank;

    private String period;

    private AnalysisStatus status;

    private FinancialState financialState;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal balance;

    private String message;

    @Builder.Default
    private List<CategoryAnalyticsResponse> categoryAnalytics = new ArrayList<>();

    @Builder.Default
    private List<InsightResponse> insights = new ArrayList<>();

    @Builder.Default
    private List<RecommendationResponse> recommendations = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}