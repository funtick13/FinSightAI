package com.finsightai.web.service.ai;

import com.finsightai.web.dto.ai.AiAnalysisRequest;
import com.finsightai.web.dto.ai.AiStatementPeriodDto;
import com.finsightai.web.dto.ai.AiStatementSummaryDto;
import com.finsightai.web.dto.ai.AiTransactionDto;
import com.finsightai.web.mapper.AiTransactionMapper;
import com.finsightai.web.model.Transaction;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.TransactionType;
import com.finsightai.web.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialAnalysisRequestBuilder {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final TransactionRepository transactionRepository;
    private final AiTransactionMapper aiTransactionMapper;

    @Transactional(readOnly = true)
    public AiAnalysisRequest build(UUID userId, String period, BankType bank) {
        YearMonth yearMonth = parsePeriod(period);

        List<Transaction> transactions = transactionRepository
                .findAllByUserIdAndPeriodAndBankOrderByDateAscTimeAsc(userId, period, bank);

        List<UUID> statementIds = transactions.stream()
                .map(Transaction::getStatement)
                .map(statement -> statement.getId())
                .distinct()
                .toList();

        List<AiTransactionDto> aiTransactions = transactions.stream()
                .map(aiTransactionMapper::toDto)
                .toList();

        AiStatementSummaryDto summary = buildSummary(transactions);

        return AiAnalysisRequest.builder()
                .requestId(UUID.randomUUID())
                .userId(userId)
                .period(period)
                .bank(bank.name())
                .statementIds(statementIds)
                .statementPeriod(AiStatementPeriodDto.builder()
                        .from(yearMonth.atDay(1))
                        .to(yearMonth.atEndOfMonth())
                        .build())
                .statementSummary(summary)
                .transactions(aiTransactions)
                .build();
    }

    private AiStatementSummaryDto buildSummary(List<Transaction> transactions) {
        BigDecimal totalIncome = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        return AiStatementSummaryDto.builder()
                .openingBalance(BigDecimal.ZERO)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .closingBalance(balance)
                .build();
    }

    private YearMonth parsePeriod(String period) {
        try {
            return YearMonth.parse(period, PERIOD_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Период должен быть в формате YYYY-MM", exception);
        }
    }
}
