package com.finsightai.web.controller;

import com.finsightai.web.dto.statement.TransactionResponse;
import com.finsightai.web.model.User;
import com.finsightai.web.service.transaction.TransactionServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/operations")
@AllArgsConstructor
public class TransactionController {

    private final TransactionServiceImpl transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal User user,
            @RequestParam String period
    ) {
        List<TransactionResponse> transactions =
                transactionService.getUserTransactions(user.getId(), period);

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        TransactionResponse transaction =
                transactionService.getUserTransaction(user.getId(), id);

        return ResponseEntity.ok(transaction);
    }
}