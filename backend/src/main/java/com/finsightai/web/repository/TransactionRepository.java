package com.finsightai.web.repository;

import com.finsightai.web.model.Transaction;
import com.finsightai.web.model.enums.BankType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByUserIdAndPeriodOrderByDateDescTimeDesc(
            UUID userId,
            String period
    );

    Optional<Transaction> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    List<Transaction> findAllByStatementId(
            UUID statementId
    );

    void deleteAllByStatementId(
            UUID statementId
    );

    boolean existsByUserIdAndBankAndPeriodAndDateAndTimeAndAmountAndDescription(
            UUID userId,
            BankType bank,
            String period,
            LocalDate date,
            LocalTime time,
            BigDecimal amount,
            String description
    );
}
