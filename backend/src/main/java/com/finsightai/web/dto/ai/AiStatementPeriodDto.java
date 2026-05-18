package com.finsightai.web.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AiStatementPeriodDto {

    @JsonProperty("from")
    private LocalDate from;

    private LocalDate to;
}