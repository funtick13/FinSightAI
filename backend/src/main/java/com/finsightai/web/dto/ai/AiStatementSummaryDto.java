package com.finsightai.web.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AiStatementSummaryDto {
    @JsonProperty("openingBalance")
    private BigDecimal openingBalance;

    @JsonProperty("totalIncome")
    private BigDecimal totalIncome;

    @JsonProperty("totalExpense")
    private BigDecimal totalExpense;

    @JsonProperty("closingBalance")
    private BigDecimal closingBalance;
}
