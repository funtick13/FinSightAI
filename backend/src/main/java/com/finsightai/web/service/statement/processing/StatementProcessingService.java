package com.finsightai.web.service.statement.processing;

import com.finsightai.web.dto.statement.TransactionCandidate;

import java.util.List;
import java.util.UUID;

public interface StatementProcessingService {
    void processStatement(UUID statementId);

    List<TransactionCandidate> processStatementToCandidates(UUID statementId);
}
