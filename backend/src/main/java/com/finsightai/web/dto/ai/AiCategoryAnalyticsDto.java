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
public class AiCategoryAnalyticsDto {
    private String category;
    private BigDecimal amount;
    private BigDecimal percent;

    @JsonProperty("operations_count")
    private Integer operationsCount;
}
