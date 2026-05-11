package com.finsightai.web.dto.statement;

import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.StatementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
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
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    private String errorMessage;
}