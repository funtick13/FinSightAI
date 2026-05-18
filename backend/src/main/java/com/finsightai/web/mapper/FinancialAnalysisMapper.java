package com.finsightai.web.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.finsightai.web.dto.ai.AiAnalysisResponse;
import com.finsightai.web.dto.ai.AiCategoryAnalyticsDto;
import com.finsightai.web.dto.ai.AiInsightDto;
import com.finsightai.web.dto.ai.AiRecommendationDto;
import com.finsightai.web.dto.ai.AiSummaryDto;
import com.finsightai.web.model.User;
import com.finsightai.web.model.analysis.CategoryAnalytics;
import com.finsightai.web.model.analysis.FinancialAnalysis;
import com.finsightai.web.model.analysis.Insight;
import com.finsightai.web.model.analysis.Recommendation;
import com.finsightai.web.model.analysis.enums.AnalysisStatus;
import com.finsightai.web.model.analysis.enums.FinancialState;
import com.finsightai.web.model.analysis.enums.InsightType;
import com.finsightai.web.model.analysis.enums.RecommendationType;
import com.finsightai.web.model.enums.BankType;
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
            BankType bank,
            String period
    ) {
        AiSummaryDto summary = response.getSummary();

        FinancialAnalysis analysis = FinancialAnalysis.builder()
                .requestId(response.getRequestId())
                .user(user)
                .bank(bank)
                .period(period)
                .status(parseAnalysisStatus(response.getStatus()))
                .financialState(parseFinancialState(response.getFinancialState()))
                .totalIncome(summary == null ? BigDecimal.ZERO : safeDecimal(summary.getTotalIncome()))
                .totalExpense(summary == null ? BigDecimal.ZERO : safeDecimal(summary.getTotalExpense()))
                .balance(summary == null ? BigDecimal.ZERO : safeDecimal(summary.getBalance()))
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
        if (response.getCategoryAnalytics() == null || response.getCategoryAnalytics().isEmpty()) {
            return;
        }

        Map<String, Integer> topRanks = buildTopRanks(response.getTopCategories());

        for (AiCategoryAnalyticsDto dto : response.getCategoryAnalytics()) {
            if (dto == null || isBlank(dto.getCategory())) {
                continue;
            }

            Integer topRank = topRanks.get(dto.getCategory());

            CategoryAnalytics categoryAnalytics = CategoryAnalytics.builder()
                    .category(dto.getCategory())
                    .amount(safeDecimal(dto.getAmount()))
                    .percent(safeDecimal(dto.getPercent()))
                    .operationsCount(dto.getOperationsCount() == null ? 0 : dto.getOperationsCount())
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

            if (category == null || isBlank(category.getCategory())) {
                continue;
            }

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
            if (dto == null || isBlank(dto.getText())) {
                continue;
            }

            Insight insight = Insight.builder()
                    .type(parseInsightType(dto.getType()))
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

        for (AiRecommendationDto dto : recommendationDtos) {
            if (dto == null || isBlank(dto.getText())) {
                continue;
            }

            Recommendation recommendation = Recommendation.builder()
                    .type(parseRecommendationType(dto.getType()))
                    .category(extractText(dto.getData(), "category"))
                    .text(dto.getText())
                    .value(extractRecommendationValue(dto.getData()))
                    .percent(extractDecimal(dto.getData(), "percent"))
                    .build();

            analysis.addRecommendation(recommendation);
        }
    }

    private AnalysisStatus parseAnalysisStatus(Object status) {
        if (status == null) {
            return AnalysisStatus.INTERNAL_ERROR;
        }

        try {
            return AnalysisStatus.valueOf(status.toString().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return AnalysisStatus.INTERNAL_ERROR;
        }
    }

    private FinancialState parseFinancialState(Object financialState) {
        if (financialState == null) {
            return null;
        }

        try {
            return FinancialState.valueOf(financialState.toString().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private InsightType parseInsightType(Object type) {
        if (type == null) {
            return InsightType.GENERAL;
        }

        try {
            return InsightType.valueOf(type.toString().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return InsightType.GENERAL;
        }
    }

    private RecommendationType parseRecommendationType(Object type) {
        if (type == null) {
            return RecommendationType.GENERAL_ADVICE;
        }

        try {
            return RecommendationType.valueOf(type.toString().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return RecommendationType.GENERAL_ADVICE;
        }
    }

    private BigDecimal extractRecommendationValue(JsonNode data) {
        if (data == null || data.isNull()) {
            return null;
        }

        BigDecimal value = extractDecimal(data, "value");

        if (value != null) {
            return value;
        }

        value = extractDecimal(data, "amount");

        if (value != null) {
            return value;
        }

        value = extractDecimal(data, "difference");

        if (value != null) {
            return value;
        }

        value = extractDecimal(data, "savingAmount");

        if (value != null) {
            return value;
        }

        return extractDecimal(data, "balance");
    }

    private String extractText(JsonNode data, String fieldName) {
        if (data == null || data.isNull() || !data.hasNonNull(fieldName)) {
            return null;
        }

        return data.get(fieldName).asText();
    }

    private BigDecimal extractDecimal(JsonNode data, String fieldName) {
        if (data == null || data.isNull() || !data.hasNonNull(fieldName)) {
            return null;
        }

        JsonNode node = data.get(fieldName);

        try {
            if (node.isNumber()) {
                return node.decimalValue();
            }

            if (node.isTextual()) {
                return new BigDecimal(node.asText());
            }
        } catch (NumberFormatException ex) {
            return null;
        }

        return null;
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}