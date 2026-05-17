package com.finsightai.web.mapper;

import com.finsightai.web.dto.ai.AiAnalysisResponse;
import com.finsightai.web.dto.ai.AiCategoryAnalyticsDto;
import com.finsightai.web.dto.ai.AiInsightDto;
import com.finsightai.web.dto.ai.AiRecommendationDto;
import com.finsightai.web.dto.ai.AiSummaryDto;
import com.finsightai.web.model.analysis.CategoryAnalytics;
import com.finsightai.web.model.analysis.FinancialAnalysis;
import com.finsightai.web.model.analysis.Insight;
import com.finsightai.web.model.analysis.Recommendation;
import com.finsightai.web.model.User;
import com.finsightai.web.model.analysis.enums.AnalysisStatus;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.analysis.enums.FinancialState;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FinancialAnalysisMapper {

    public FinancialAnalysis toEntity(
            AiAnalysisResponse response,
            User user,
            BankType bank
    ) {
        AiSummaryDto summary = response.getSummary();

        FinancialAnalysis analysis = FinancialAnalysis.builder()
                .requestId(response.getRequestId())
                .user(user)
                .bank(bank)
                .period(response.getPeriod())
                .status(parseAnalysisStatus(response.getStatus()))
                .financialState(parseFinancialState(response.getFinancialState()))
                .totalIncome(summary == null ? BigDecimal.ZERO : summary.getTotalIncome())
                .totalExpense(summary == null ? BigDecimal.ZERO : summary.getTotalExpense())
                .balance(summary == null ? BigDecimal.ZERO : summary.getBalance())
                .message(response.getMessage())
                .build();

        addCategoryAnalytics(analysis, response);
        addInsights(analysis, response.getInsights());
        addRecommendations(analysis, response.getRecommendations());

        return analysis;
    }

    private void addCategoryAnalytics(
            FinancialAnalysis analysis,
            AiAnalysisResponse response
    ) {
        Map<String, Integer> topRanks = buildTopRanks(response.getTopCategories());

        for (AiCategoryAnalyticsDto dto : response.getCategoryAnalytics()) {
            Integer topRank = topRanks.get(dto.getCategory());

            CategoryAnalytics categoryAnalytics = CategoryAnalytics.builder()
                    .category(dto.getCategory())
                    .amount(dto.getAmount())
                    .percent(dto.getPercent())
                    .operationsCount(dto.getOperationsCount())
                    .isTop(topRank != null)
                    .topRank(topRank)
                    .build();

            analysis.addCategoryAnalytics(categoryAnalytics);
        }
    }

    private Map<String, Integer> buildTopRanks(List<AiCategoryAnalyticsDto> topCategories) {
        Map<String, Integer> ranks = new HashMap<>();

        if (topCategories == null || topCategories.isEmpty()) {
            return ranks;
        }

        for (int i = 0; i < topCategories.size(); i++) {
            AiCategoryAnalyticsDto category = topCategories.get(i);
            ranks.put(category.getCategory(), i + 1);
        }

        return ranks;
    }

    private void addInsights(
            FinancialAnalysis analysis,
            List<AiInsightDto> insightDtos
    ) {
        if (insightDtos == null || insightDtos.isEmpty()) {
            return;
        }

        for (AiInsightDto dto : insightDtos) {
            Insight insight = Insight.builder()
                    .type(dto.getType())
                    .text(dto.getText())
                    .build();

            analysis.addInsight(insight);
        }
    }

    private void addRecommendations(
            FinancialAnalysis analysis,
            List<AiRecommendationDto> recommendationDtos
    ) {
        if (recommendationDtos == null || recommendationDtos.isEmpty()) {
            return;
        }

//        for (AiRecommendationDto dto : recommendationDtos) {
//            Recommendation recommendation = Recommendation.builder()
//                    .type(dto.getType())
//                    .category(dto.getCategory())
//                    .text(dto.getText())
//                    .value(dto.getValue())
//                    .percent(dto.getPercent())
//                    .build();
//
//            analysis.addRecommendation(recommendation);
//        }
    }

    private AnalysisStatus parseAnalysisStatus(Object status) {
        if (status == null) {
            return AnalysisStatus.INTERNAL_ERROR;
        }

        return AnalysisStatus.valueOf(status.toString());
    }

    private FinancialState parseFinancialState(Object financialState) {
        if (financialState == null) {
            return null;
        }

        return FinancialState.valueOf(financialState.toString());
    }
}