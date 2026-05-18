package com.finsightai.web.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AiAnalysisRequest {

    @JsonProperty("requestId")
    private UUID requestId;

    @JsonProperty("userId")
    private UUID userId;

    private String period;

    private String bank;

    @Builder.Default
    @JsonProperty("statementIds")
    private List<UUID> statementIds = new ArrayList<>();

    @JsonProperty("statementPeriod")
    private AiStatementPeriodDto statementPeriod;

    @JsonProperty("statementSummary")
    private AiStatementSummaryDto statementSummary;

    @Builder.Default
    private List<AiTransactionDto> transactions = new ArrayList<>();
}