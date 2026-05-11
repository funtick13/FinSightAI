package com.finsightai.web.repository;

import com.finsightai.web.model.Statement;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.StatementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StatementRepository extends JpaRepository<Statement, UUID> {

    Page<Statement> findAllByUserId(UUID userId, Pageable pageable);

    Page<Statement> findAllByUserIdAndPeriod(UUID userId, String period, Pageable pageable);

    Page<Statement> findAllByUserIdAndBank(UUID userId, BankType bank, Pageable pageable);

    Page<Statement> findAllByUserIdAndStatus(UUID userId, StatementStatus status, Pageable pageable);

    Optional<Statement> findByIdAndUserId(UUID id, UUID userId);
}