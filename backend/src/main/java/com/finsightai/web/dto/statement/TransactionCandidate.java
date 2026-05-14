package com.finsightai.web.dto.statement;

import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TransactionCandidate(
        LocalDate date,
        LocalTime time,
        BigDecimal amount,
        TransactionType type,
        String category,
        String description,
        BankType bank,
        String period,
        UUID userId,
        UUID statementId
) {
}
