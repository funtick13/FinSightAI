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
public class AiAnalysisRequest {
    @JsonProperty("requestId")
    private UUID requestId;

    @JsonProperty("userId")
    private UUID userId;

    private String period;
    private String bank;

    @JsonProperty("statementIds")
    private List<UUID> statementIds;

    @JsonProperty("statementPeriod")
    private AiStatementPeriodDto statementPeriod;

    @JsonProperty("statementSummary")
    private AiStatementSummaryDto statementSummary;

    private List<AiTransactionDto> transactions;
}
