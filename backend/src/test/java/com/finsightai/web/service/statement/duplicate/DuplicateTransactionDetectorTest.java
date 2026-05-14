package com.finsightai.web.service.statement.duplicate;

import com.finsightai.web.dto.statement.TransactionCandidate;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DuplicateTransactionDetectorTest {

    private final DuplicateTransactionDetector detector = new DuplicateTransactionDetector();

    @Test
    void removeDuplicatesKeepsFirstCandidateWithSameMvpKey() {
        UUID userId = UUID.randomUUID();
        UUID statementId = UUID.randomUUID();
        TransactionCandidate first = candidate(userId, statementId, "Такси");
        TransactionCandidate duplicate = candidate(userId, statementId, "Такси");
        TransactionCandidate differentDescription = new TransactionCandidate(
                first.date(),
                first.time(),
                first.amount(),
                first.type(),
                first.category(),
                "Другая поездка",
                first.bank(),
                first.period(),
                first.userId(),
                first.statementId()
        );

        List<TransactionCandidate> result = detector.removeDuplicates(List.of(first, duplicate, differentDescription));

        assertEquals(2, result.size());
        assertEquals(first, result.get(0));
        assertEquals(differentDescription, result.get(1));
    }

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
