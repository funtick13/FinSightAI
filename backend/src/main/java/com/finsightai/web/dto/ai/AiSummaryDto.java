package com.finsightai.web.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AiSummaryDto {

    @JsonProperty("totalIncome")
    private BigDecimal totalIncome;

    @JsonProperty("totalExpense")
    private BigDecimal totalExpense;

    private BigDecimal balance;

    private String period;
}