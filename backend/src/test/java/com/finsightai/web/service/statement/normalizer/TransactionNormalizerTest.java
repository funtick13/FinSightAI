package com.finsightai.web.service.statement.normalizer;

import com.finsightai.web.dto.statement.ParsedTransaction;
import com.finsightai.web.dto.statement.TransactionCandidate;
import com.finsightai.web.exception.StatementParsingException;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionNormalizerTest {

    private final TransactionNormalizer normalizer = new TransactionNormalizer();

    @Test
    void normalizeConvertsExpenseWithoutPlusToNegativeAmount() {
        UUID userId = UUID.randomUUID();
        UUID statementId = UUID.randomUUID();
        ParsedTransaction parsedTransaction = new ParsedTransaction(
                "01.04.2026",
                null,
                "",
                "  Покупка   продуктов ",
                "1 234,56 ₽",
                null
        );

        TransactionCandidate result = normalizer.normalize(
                parsedTransaction,
                userId,
                statementId,
                BankType.SBERBANK,
                "2026-04"
        );

        assertEquals(LocalDate.of(2026, 4, 1), result.getDate());
        assertEquals(LocalTime.MIDNIGHT, result.getTime());
        assertEquals(new BigDecimal("-1234.56"), result.getAmount());
        assertEquals(TransactionType.EXPENSE, result.getType());
        assertEquals("Прочее", result.getCategory());
        assertEquals("Покупка продуктов", result.getDescription());
        assertEquals(userId, result.getUserId());
        assertEquals(statementId, result.getStatementId());
    }

    @Test
    void normalizeConvertsIncomeWithPlusToPositiveAmount() {
        ParsedTransaction parsedTransaction = new ParsedTransaction(
                "02.04.2026",
                "09:15",
                "Переводы",
                "Зачисление",
                "+5 000,00 ₽",
                null
        );

        TransactionCandidate result = normalizer.normalize(
                parsedTransaction,
                UUID.randomUUID(),
                UUID.randomUUID(),
                BankType.SBERBANK,
                "2026-04"
        );

        assertEquals(LocalTime.of(9, 15), result.getTime());
        assertEquals(new BigDecimal("5000.00"), result.getAmount());
        assertEquals(TransactionType.INCOME, result.getType());
    }

    @Test
    void normalizeThrowsWhenDateCannotBeParsed() {
        ParsedTransaction parsedTransaction = new ParsedTransaction(
                "не дата",
                null,
                "Прочее",
                "Описание",
                "100,00 ₽",
                null
        );

        assertThrows(StatementParsingException.class, () -> normalizer.normalize(
                parsedTransaction,
                UUID.randomUUID(),
                UUID.randomUUID(),
                BankType.SBERBANK,
                "2026-04"
        ));
    }
}
