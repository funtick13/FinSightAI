package com.finsightai.web.dto.analysis;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CategoryAnalyticsResponse {

    private UUID id;

    private String category;

    private BigDecimal amount;

    private BigDecimal percent;

    private Integer operationsCount;

    private Boolean isTop;

    private Integer topRank;
}