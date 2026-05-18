package com.finsightai.web.service.analysis;

import com.finsightai.web.dto.ai.AiAnalysisRequest;
import com.finsightai.web.dto.ai.AiAnalysisResponse;
import com.finsightai.web.mapper.FinancialAnalysisMapper;
import com.finsightai.web.model.User;
import com.finsightai.web.model.analysis.FinancialAnalysis;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.repository.FinancialAnalysisRepository;
import com.finsightai.web.repository.UserRepository;
import com.finsightai.web.service.ai.AiAnalysisClient;
import com.finsightai.web.service.ai.FinancialAnalysisRequestBuilder;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialAnalysisService {

    private final FinancialAnalysisRequestBuilder requestBuilder;
    private final AiAnalysisClient aiAnalysisClient;
    private final FinancialAnalysisRepository financialAnalysisRepository;
    private final FinancialAnalysisMapper financialAnalysisMapper;
    private final UserRepository userRepository;

    @Transactional
    public void analyzePeriod(UUID userId, String period, BankType bank) {
        AiAnalysisRequest request = requestBuilder.build(userId, period, bank);

        if (request.getTransactions() == null || request.getTransactions().isEmpty()) {
            financialAnalysisRepository.deleteAllByUserIdAndPeriodAndBank(
                    userId,
                    period,
                    bank
            );

            log.info(
                    "AI analysis skipped and old analysis deleted: userId={}, period={}, bank={}, reason=no transactions",
                    userId,
                    period,
                    bank
            );
            return;
        }

        try {
            AiAnalysisResponse response = aiAnalysisClient.analyze(request);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

            financialAnalysisRepository.deleteAllByUserIdAndPeriodAndBank(
                    userId,
                    period,
                    bank
            );

            FinancialAnalysis analysis = financialAnalysisMapper.toEntity(
                    response,
                    user,
                    bank,
                    period
            );

            FinancialAnalysis savedAnalysis = financialAnalysisRepository.save(analysis);

            log.info(
                    "AI analysis saved: analysisId={}, requestId={}, userId={}, period={}, aiPeriod={}, status={}, financialState={}, recommendations={}, insights={}",
                    savedAnalysis.getId(),
                    response.getRequestId(),
                    userId,
                    period,
                    response.getPeriod(),
                    response.getStatus(),
                    response.getFinancialState(),
                    savedAnalysis.getRecommendations() == null ? 0 : savedAnalysis.getRecommendations().size(),
                    savedAnalysis.getInsights() == null ? 0 : savedAnalysis.getInsights().size()
            );
        } catch (RuntimeException exception) {
            log.warn(
                    "AI analysis failed: requestId={}, userId={}, period={}, bank={}, reason={}",
                    request.getRequestId(),
                    userId,
                    period,
                    bank,
                    exception.getMessage()
            );
            throw exception;
        }
    }
}