package com.finsightai.web.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
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
    @Builder.Default
    private List<AiCategoryAnalyticsDto> categoryAnalytics = new ArrayList<>();

    @JsonProperty("topCategories")
    @Builder.Default
    private List<AiCategoryAnalyticsDto> topCategories = new ArrayList<>();

    @Builder.Default
    private List<AiInsightDto> insights = new ArrayList<>();

    @Builder.Default
    private List<AiRecommendationDto> recommendations = new ArrayList<>();

    private String message;
}