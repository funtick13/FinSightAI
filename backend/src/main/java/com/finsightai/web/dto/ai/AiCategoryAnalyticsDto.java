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
public class AiCategoryAnalyticsDto {

    private String category;

    private BigDecimal amount;

    private BigDecimal percent;

    @JsonProperty("operations_count")
    private Integer operationsCount;
}