package com.finsightai.web.service.ai;

import com.finsightai.web.dto.ai.AiAnalysisRequest;
import com.finsightai.web.dto.ai.AiAnalysisResponse;
import com.finsightai.web.dto.ai.AiTransactionDto;
import com.finsightai.web.model.enums.BankType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialAnalysisServiceTest {

    @Mock
    private FinancialAnalysisRequestBuilder requestBuilder;

    @Mock
    private AiAnalysisClient aiAnalysisClient;

    @InjectMocks
    private FinancialAnalysisService financialAnalysisService;

    @Test
    void analyzePeriodSkipsAiClientWhenTransactionsAreEmpty() {
        UUID userId = UUID.randomUUID();
        AiAnalysisRequest request = request(userId, List.of());
        when(requestBuilder.build(userId, "2026-04", BankType.SBERBANK)).thenReturn(request);

        financialAnalysisService.analyzePeriod(userId, "2026-04", BankType.SBERBANK);

        verify(aiAnalysisClient, never()).analyze(request);
    }

    @Test
    void analyzePeriodCallsAiClientWhenTransactionsExist() {
        UUID userId = UUID.randomUUID();
        AiAnalysisRequest request = request(userId, List.of(AiTransactionDto.builder().id(UUID.randomUUID()).build()));
        when(requestBuilder.build(userId, "2026-04", BankType.SBERBANK)).thenReturn(request);
        when(aiAnalysisClient.analyze(request)).thenReturn(AiAnalysisResponse.builder()
                .requestId(request.getRequestId())
                .userId(userId)
                .period("2026-04")
                .status("SUCCESS")
                .financialState("STABLE")
                .recommendations(List.of())
                .insights(List.of())
                .build());

        financialAnalysisService.analyzePeriod(userId, "2026-04", BankType.SBERBANK);

        verify(aiAnalysisClient).analyze(request);
    }

    @Test
    void analyzePeriodRethrowsAiClientFailure() {
        UUID userId = UUID.randomUUID();
        AiAnalysisRequest request = request(userId, List.of(AiTransactionDto.builder().id(UUID.randomUUID()).build()));
        when(requestBuilder.build(userId, "2026-04", BankType.SBERBANK)).thenReturn(request);
        when(aiAnalysisClient.analyze(request)).thenThrow(new RuntimeException("AI failed"));

        assertThrows(RuntimeException.class, () ->
                financialAnalysisService.analyzePeriod(userId, "2026-04", BankType.SBERBANK)
        );
    }

    private AiAnalysisRequest request(UUID userId, List<AiTransactionDto> transactions) {
        return AiAnalysisRequest.builder()
                .requestId(UUID.randomUUID())
                .userId(userId)
                .period("2026-04")
                .bank("SBERBANK")
                .statementIds(List.of(UUID.randomUUID()))
                .transactions(transactions)
                .build();
    }
}
