package com.finsightai.web.mapper;

import com.finsightai.web.dto.analysis.CategoryAnalyticsResponse;
import com.finsightai.web.dto.analysis.FinancialAnalysisResponse;
import com.finsightai.web.dto.analysis.InsightResponse;
import com.finsightai.web.dto.analysis.RecommendationResponse;
import com.finsightai.web.model.analysis.CategoryAnalytics;
import com.finsightai.web.model.analysis.FinancialAnalysis;
import com.finsightai.web.model.analysis.Insight;
import com.finsightai.web.model.analysis.Recommendation;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class FinancialAnalysisResponseMapper {

    public FinancialAnalysisResponse toResponse(FinancialAnalysis analysis) {
        if (analysis == null) {
            return null;
        }

        return FinancialAnalysisResponse.builder()
                .id(analysis.getId())
                .requestId(analysis.getRequestId())
                .bank(analysis.getBank())
                .period(analysis.getPeriod())
                .status(analysis.getStatus())
                .financialState(analysis.getFinancialState())
                .totalIncome(analysis.getTotalIncome())
                .totalExpense(analysis.getTotalExpense())
                .balance(analysis.getBalance())
                .message(analysis.getMessage())
                .categoryAnalytics(toCategoryAnalyticsResponses(analysis.getCategoryAnalytics()))
                .insights(toInsightResponses(analysis.getInsights()))
                .recommendations(toRecommendationResponses(analysis.getRecommendations()))
                .createdAt(analysis.getCreatedAt())
                .updatedAt(analysis.getUpdatedAt())
                .build();
    }

    public List<FinancialAnalysisResponse> toResponses(List<FinancialAnalysis> analyses) {
        if (analyses == null || analyses.isEmpty()) {
            return List.of();
        }

        return analyses.stream()
                .map(this::toResponse)
                .toList();
    }

    private List<CategoryAnalyticsResponse> toCategoryAnalyticsResponses(
            List<CategoryAnalytics> categories
    ) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }

        return categories.stream()
                .sorted(
                        Comparator
                                .comparing(CategoryAnalytics::getIsTop, Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(
                                        CategoryAnalytics::getTopRank,
                                        Comparator.nullsLast(Integer::compareTo)
                                )
                                .thenComparing(CategoryAnalytics::getAmount, Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .map(this::toCategoryAnalyticsResponse)
                .toList();
    }

    private CategoryAnalyticsResponse toCategoryAnalyticsResponse(CategoryAnalytics category) {
        return CategoryAnalyticsResponse.builder()
                .id(category.getId())
                .category(category.getCategory())
                .amount(category.getAmount())
                .percent(category.getPercent())
                .operationsCount(category.getOperationsCount())
                .isTop(category.getIsTop())
                .topRank(category.getTopRank())
                .build();
    }

    private List<InsightResponse> toInsightResponses(List<Insight> insights) {
        if (insights == null || insights.isEmpty()) {
            return List.of();
        }

        return insights.stream()
                .map(this::toInsightResponse)
                .toList();
    }

    private InsightResponse toInsightResponse(Insight insight) {
        return InsightResponse.builder()
                .id(insight.getId())
                .type(insight.getType())
                .text(insight.getText())
                .createdAt(insight.getCreatedAt())
                .build();
    }

    private List<RecommendationResponse> toRecommendationResponses(
            List<Recommendation> recommendations
    ) {
        if (recommendations == null || recommendations.isEmpty()) {
            return List.of();
        }

        return recommendations.stream()
                .map(this::toRecommendationResponse)
                .toList();
    }

    private RecommendationResponse toRecommendationResponse(Recommendation recommendation) {
        return RecommendationResponse.builder()
                .id(recommendation.getId())
                .type(recommendation.getType())
                .category(recommendation.getCategory())
                .text(recommendation.getText())
                .value(recommendation.getValue())
                .percent(recommendation.getPercent())
                .createdAt(recommendation.getCreatedAt())
                .build();
    }
}