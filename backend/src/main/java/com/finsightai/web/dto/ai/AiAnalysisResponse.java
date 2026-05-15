package com.finsightai.web.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AiAnalysisResponse {
    @JsonProperty("requestId")
    private UUID requestId;

    @JsonProperty("userId")
    private UUID userId;

    private String period;
    private String status;
    private AiSummaryDto summary;

    @JsonProperty("financialState")
    private String financialState;

    @JsonProperty("categoryAnalytics")
    private List<AiCategoryAnalyticsDto> categoryAnalytics;

    @JsonProperty("topCategories")
    private List<AiCategoryAnalyticsDto> topCategories;

    private List<AiInsightDto> insights;
    private List<AiRecommendationDto> recommendations;
    private String message;
}
