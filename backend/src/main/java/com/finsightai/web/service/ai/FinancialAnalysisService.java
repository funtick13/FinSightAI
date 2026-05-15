package com.finsightai.web.service.ai;

import com.finsightai.web.dto.ai.AiAnalysisRequest;
import com.finsightai.web.dto.ai.AiAnalysisResponse;
import com.finsightai.web.model.enums.BankType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialAnalysisService {

    private final FinancialAnalysisRequestBuilder requestBuilder;
    private final AiAnalysisClient aiAnalysisClient;

    public void analyzePeriod(UUID userId, String period, BankType bank) {
        AiAnalysisRequest request = requestBuilder.build(userId, period, bank);

        if (request.getTransactions() == null || request.getTransactions().isEmpty()) {
            log.info(
                    "AI analysis skipped: userId={}, period={}, bank={}, reason=no transactions",
                    userId,
                    period,
                    bank
            );
            return;
        }

        try {
            AiAnalysisResponse response = aiAnalysisClient.analyze(request);
            log.info(
                    "AI analysis completed: requestId={}, userId={}, period={}, status={}, financialState={}, recommendations={}, insights={}",
                    response.getRequestId(),
                    response.getUserId(),
                    response.getPeriod(),
                    response.getStatus(),
                    response.getFinancialState(),
                    response.getRecommendations() == null ? 0 : response.getRecommendations().size(),
                    response.getInsights() == null ? 0 : response.getInsights().size()
            );
        } catch (RuntimeException exception) {
            log.warn(
                    "AI analysis failed: requestId={}, userId={}, period={}, reason={}",
                    request.getRequestId(),
                    userId,
                    period,
                    exception.getMessage()
            );
            throw exception;
        }
    }
}
