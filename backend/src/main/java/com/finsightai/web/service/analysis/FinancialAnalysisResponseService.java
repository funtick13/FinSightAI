package com.finsightai.web.service.analysis;

import com.finsightai.web.dto.analysis.FinancialAnalysisResponse;
import com.finsightai.web.mapper.FinancialAnalysisResponseMapper;
import com.finsightai.web.model.analysis.FinancialAnalysis;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.repository.FinancialAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialAnalysisResponseService {

    private final FinancialAnalysisRepository financialAnalysisRepository;
    private final FinancialAnalysisResponseMapper financialAnalysisResponseMapper;

    @Transactional(readOnly = true)
    public FinancialAnalysisResponse getLatestAnalysis(
            UUID userId,
            String period,
            BankType bank
    ) {
        FinancialAnalysis analysis = financialAnalysisRepository
                .findFirstByUserIdAndPeriodAndBankOrderByCreatedAtDesc(
                        userId,
                        period,
                        bank
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Анализ за указанный период не найден"
                ));

        return financialAnalysisResponseMapper.toResponse(analysis);
    }

    @Transactional(readOnly = true)
    public List<FinancialAnalysisResponse> getAnalysisHistory(UUID userId) {
        List<FinancialAnalysis> analyses = financialAnalysisRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId);

        return financialAnalysisResponseMapper.toResponses(analyses);
    }

    @Transactional(readOnly = true)
    public List<FinancialAnalysisResponse> getAnalysisHistoryByPeriod(
            UUID userId,
            String period
    ) {
        List<FinancialAnalysis> analyses = financialAnalysisRepository
                .findAllByUserIdAndPeriodOrderByCreatedAtDesc(userId, period);

        return financialAnalysisResponseMapper.toResponses(analyses);
    }

    @Transactional(readOnly = true)
    public FinancialAnalysisResponse getAnalysisById(
            UUID userId,
            UUID analysisId
    ) {
        FinancialAnalysis analysis = financialAnalysisRepository
                .findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Анализ не найден"
                ));

        return financialAnalysisResponseMapper.toResponse(analysis);
    }
}