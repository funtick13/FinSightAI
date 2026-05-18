package com.finsightai.web.dto.statement;

import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.StatementStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementResponse {

    private UUID id;

    private BankType bank;

    private String period;

    private String originalFileName;

    private Long fileSize;

    private StatementStatus status;

    private LocalDateTime uploadedAt;

    private LocalDateTime processedAt;

    private String errorMessage;
}