package com.finsightai.web.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.finsightai.web.dto.ai.AiAnalysisRequest;
import com.finsightai.web.mapper.AiTransactionMapper;
import com.finsightai.web.model.Statement;
import com.finsightai.web.model.Transaction;
import com.finsightai.web.model.User;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.TransactionType;
import com.finsightai.web.repository.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FinancialAnalysisRequestBuilderTest {

    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final FinancialAnalysisRequestBuilder builder =
            new FinancialAnalysisRequestBuilder(transactionRepository, new AiTransactionMapper());

    @Test
    void buildCreatesAiAnalysisRequestWithSummaryAndCamelCaseJson() throws Exception {
        UUID userId = UUID.randomUUID();
        Statement statement = statement(userId);
        Transaction income = transaction(statement, TransactionType.INCOME, new BigDecimal("500.00"), "Пополнение");
        Transaction expense = transaction(statement, TransactionType.EXPENSE, new BigDecimal("-125.50"), "Покупка");

        when(transactionRepository.findAllByUserIdAndPeriodAndBankOrderByDateAscTimeAsc(
                userId,
                "2026-04",
                BankType.SBERBANK
        )).thenReturn(List.of(income, expense));

        AiAnalysisRequest result = builder.build(userId, "2026-04", BankType.SBERBANK);

        assertNotNull(result.getRequestId());
        assertEquals(userId, result.getUserId());
        assertEquals("2026-04", result.getPeriod());
        assertEquals("SBERBANK", result.getBank());
        assertEquals(List.of(statement.getId()), result.getStatementIds());
        assertEquals(LocalDate.of(2026, 4, 1), result.getStatementPeriod().getFrom());
        assertEquals(LocalDate.of(2026, 4, 30), result.getStatementPeriod().getTo());
        assertEquals(new BigDecimal("500.00"), result.getStatementSummary().getTotalIncome());
        assertEquals(new BigDecimal("125.50"), result.getStatementSummary().getTotalExpense());
        assertEquals(new BigDecimal("374.50"), result.getStatementSummary().getClosingBalance());
        assertEquals(2, result.getTransactions().size());
        assertEquals("12:30", result.getTransactions().get(0).getTime());

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JsonNode json = objectMapper.valueToTree(result);
        assertNotNull(json.get("requestId"));
        assertNotNull(json.get("userId"));
        assertNotNull(json.get("statementIds"));
        assertEquals("2026-04-01", json.get("statementPeriod").get("from").asText());
        assertNotNull(json.get("statementSummary").get("totalIncome"));
    }

    private Statement statement(UUID userId) {
        User user = new User();
        user.setId(userId);

        return Statement.builder()
                .id(UUID.randomUUID())
                .user(user)
                .bank(BankType.SBERBANK)
                .period("2026-04")
                .originalFileName("statement.pdf")
                .storedFileName("statement.pdf")
                .filePath("uploads/statements/test.pdf")
                .contentType("application/pdf")
                .fileSize(100L)
                .uploadedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Transaction transaction(Statement statement, TransactionType type, BigDecimal amount, String description) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .user(statement.getUser())
                .statement(statement)
                .bank(statement.getBank())
                .period(statement.getPeriod())
                .date(LocalDate.of(2026, 4, 16))
                .time(LocalTime.of(12, 30))
                .type(type)
                .amount(amount)
                .category("Прочее")
                .description(description)
                .build();
    }
}
