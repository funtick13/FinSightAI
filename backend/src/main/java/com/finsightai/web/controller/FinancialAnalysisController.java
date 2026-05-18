package com.finsightai.web.controller;

import com.finsightai.web.dto.analysis.FinancialAnalysisResponse;
import com.finsightai.web.model.User;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.service.analysis.FinancialAnalysisResponseService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analysis")
@AllArgsConstructor
public class FinancialAnalysisController {

    private final FinancialAnalysisResponseService financialAnalysisResponseService;

    @GetMapping("/latest")
    public ResponseEntity<FinancialAnalysisResponse> getLatestFinancialAnalysis(
            @AuthenticationPrincipal User user,
            @RequestParam String period,
            @RequestParam(defaultValue = "SBERBANK") BankType bank
            ) {
        FinancialAnalysisResponse financialAnalysisResponse = financialAnalysisResponseService.getLatestAnalysis(
                user.getId(),
                period,
                bank
        );
        return ResponseEntity.ok(financialAnalysisResponse);
    }

    @GetMapping("/history")
    public ResponseEntity<List<FinancialAnalysisResponse>> getAnalysisHistory(
            @AuthenticationPrincipal User user,
            @RequestParam String period
    ) {
        List<FinancialAnalysisResponse> response;

        if (period == null || period.isBlank()) {
            response = financialAnalysisResponseService.getAnalysisHistory(user.getId());
        } else {
            response = financialAnalysisResponseService.getAnalysisHistoryByPeriod(
                    user.getId(),
                    period
            );
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinancialAnalysisResponse> getAnalysisById(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        FinancialAnalysisResponse response = financialAnalysisResponseService.getAnalysisById(
                user.getId(),
                id
        );

        return ResponseEntity.ok(response);
    }
}
