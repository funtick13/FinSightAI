package com.finsightai.web.service.statement.duplicate;

import com.finsightai.web.dto.transaction.TransactionCandidate;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DuplicateTransactionDetectorTest {

    private final DuplicateTransactionDetector detector = new DuplicateTransactionDetector();



    private TransactionCandidate candidate(UUID userId, UUID statementId, String description) {
        return new TransactionCandidate(
                LocalDate.of(2026, 4, 1),
                LocalTime.of(12, 30),
                new BigDecimal("-450.00"),
                TransactionType.EXPENSE,
                "Транспорт",
                description,
                BankType.SBERBANK,
                "2026-04",
                userId,
                statementId
        );
    }
}
