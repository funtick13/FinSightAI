package com.finsightai.web.service.transaction;

import com.finsightai.web.dto.transaction.TransactionCandidate;
import com.finsightai.web.dto.transaction.TransactionResponse;
import com.finsightai.web.model.Transaction;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    List<Transaction> saveCandidates(List<TransactionCandidate> candidates);

    List<TransactionResponse> getUserTransactions(UUID userId, String period);

    TransactionResponse getUserTransaction(UUID userId, UUID transactionId);

    void deleteByStatementId(UUID statementId);
}