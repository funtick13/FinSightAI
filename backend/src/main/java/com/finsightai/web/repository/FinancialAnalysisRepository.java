package com.finsightai.web.repository;

import com.finsightai.web.model.analysis.FinancialAnalysis;
import com.finsightai.web.model.analysis.enums.AnalysisStatus;
import com.finsightai.web.model.enums.BankType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialAnalysisRepository extends JpaRepository<FinancialAnalysis, UUID> {

    List<FinancialAnalysis> findAllByUserIdOrderByCreatedAtDesc(
            UUID userId
    );

    List<FinancialAnalysis> findAllByUserIdAndPeriodOrderByCreatedAtDesc(
            UUID userId,
            String period
    );

    Optional<FinancialAnalysis> findFirstByUserIdAndPeriodOrderByCreatedAtDesc(
            UUID userId,
            String period
    );

    Optional<FinancialAnalysis> findFirstByUserIdAndPeriodAndBankOrderByCreatedAtDesc(
            UUID userId,
            String period,
            BankType bank
    );

    Optional<FinancialAnalysis> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    Optional<FinancialAnalysis> findByRequestId(
            UUID requestId
    );

    boolean existsByUserIdAndPeriodAndBankAndStatus(
            UUID userId,
            String period,
            BankType bank,
            AnalysisStatus status
    );

    void deleteAllByUserIdAndPeriodAndBank(
            UUID userId,
            String period,
            BankType bank
    );
}