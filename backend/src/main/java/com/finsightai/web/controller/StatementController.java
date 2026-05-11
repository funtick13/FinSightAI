package com.finsightai.web.controller;

import com.finsightai.web.dto.statement.StatementResponse;
import com.finsightai.web.model.User;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.service.statement.StatementService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/statements")
@AllArgsConstructor
public class StatementController {
    private StatementService statementService;

    @PostMapping
    public ResponseEntity<StatementResponse> loadStatement(
            @AuthenticationPrincipal User user,
            @RequestParam MultipartFile file,
            @RequestParam String period,
            @RequestParam BankType bank) {

        StatementResponse statementResponse = statementService.uploadStatement(
                user,
                bank,
                period,
                file
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(statementResponse);
    }

    @GetMapping
    public ResponseEntity<Page<StatementResponse>> getStatement(
            @AuthenticationPrincipal User user,
            Pageable pageable) {

        Page<StatementResponse> statementResponse = statementService.getUserStatements(
                user.getId(),
                pageable
        );

        return ResponseEntity.status(HttpStatus.OK).body(statementResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StatementResponse> getStatementById(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        StatementResponse statementResponse = statementService.getStatement(
                user.getId(),
                id
        );

        return ResponseEntity.status(HttpStatus.OK).body(statementResponse);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<StatementResponse> deleteStatement(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {

        statementService.deleteStatement(
                user.getId(),
                id
        );

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
