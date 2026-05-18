package com.finsightai.web.dto.analysis;

import com.finsightai.web.model.analysis.enums.RecommendationType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RecommendationResponse {

    private UUID id;

    private RecommendationType type;

    private String category;

    private String text;

    private BigDecimal value;

    private BigDecimal percent;

    private LocalDateTime createdAt;
}