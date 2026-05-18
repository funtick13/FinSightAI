package com.finsightai.web.mapper;

import com.finsightai.web.dto.transaction.TransactionCandidate;
import com.finsightai.web.dto.transaction.TransactionResponse;
import com.finsightai.web.model.Statement;
import com.finsightai.web.model.Transaction;
import com.finsightai.web.model.User;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(
            TransactionCandidate candidate,
            User user,
            Statement statement
    ) {
        return Transaction.builder()
                .user(user)
                .statement(statement)
                .bank(candidate.getBank())
                .period(candidate.getPeriod())
                .date(candidate.getDate())
                .time(candidate.getTime())
                .type(candidate.getType())
                .amount(candidate.getAmount())
                .category(candidate.getCategory())
                .description(candidate.getDescription())
                .build();
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .statementId(transaction.getStatement().getId())
                .bank(transaction.getBank())
                .period(transaction.getPeriod())
                .date(transaction.getDate())
                .time(transaction.getTime())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .category(transaction.getCategory())
                .description(transaction.getDescription())
                .build();
    }
}
