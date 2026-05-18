package com.finsightai.web.dto.analysis;

import com.finsightai.web.model.analysis.enums.InsightType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InsightResponse {

    private UUID id;

    private InsightType type;

    private String text;

    private LocalDateTime createdAt;
}