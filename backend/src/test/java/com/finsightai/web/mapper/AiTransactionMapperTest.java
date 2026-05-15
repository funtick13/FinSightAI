package com.finsightai.web.mapper;

import com.finsightai.web.dto.ai.AiTransactionDto;
import com.finsightai.web.model.Transaction;
import com.finsightai.web.model.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AiTransactionMapperTest {

    private final AiTransactionMapper mapper = new AiTransactionMapper();

    @Test
    void toDtoFormatsTimeAsHourAndMinute() {
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .date(LocalDate.of(2026, 4, 16))
                .time(LocalTime.of(14, 25, 30))
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("-1250.00"))
                .category("Продукты")
                .description(" Покупка   в магазине ")
                .build();

        AiTransactionDto result = mapper.toDto(transaction);

        assertEquals(transactionId, result.getId());
        assertEquals(LocalDate.of(2026, 4, 16), result.getDate());
        assertEquals("14:25", result.getTime());
        assertEquals(TransactionType.EXPENSE, result.getType());
        assertEquals(new BigDecimal("-1250.00"), result.getAmount());
        assertEquals("Продукты", result.getCategory());
        assertEquals("Покупка в магазине", result.getDescription());
    }

    @Test
    void toDtoAllowsNullTimeAndDefaultsBlankCategory() {
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .date(LocalDate.of(2026, 4, 16))
                .time(null)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100.00"))
                .category(" ")
                .description(" ")
                .build();

        AiTransactionDto result = mapper.toDto(transaction);

        assertNull(result.getTime());
        assertEquals("Прочее", result.getCategory());
        assertNull(result.getDescription());
    }
}
