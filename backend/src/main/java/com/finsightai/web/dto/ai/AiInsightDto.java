package com.finsightai.web.dto.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.finsightai.web.model.analysis.enums.InsightType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AiInsightDto {
    private String text;
    private InsightType type;
    private JsonNode data;
}
