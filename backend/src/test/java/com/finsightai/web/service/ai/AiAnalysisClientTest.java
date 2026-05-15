package com.finsightai.web.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsightai.web.dto.ai.AiAnalysisRequest;
import com.finsightai.web.dto.ai.AiAnalysisResponse;
import com.finsightai.web.dto.ai.AiStatementPeriodDto;
import com.finsightai.web.dto.ai.AiStatementSummaryDto;
import com.finsightai.web.exception.AiServiceException;
import com.finsightai.web.configuration.AiServiceClientConfig;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiAnalysisClientTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void analyzePostsRequestToAnalysisEndpoint() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AtomicReference<String> requestBody = new AtomicReference<>();
        startServer(200, responseJson(requestId, userId), requestBody);

        AiAnalysisClient client = new AiAnalysisClient(webClient());

        AiAnalysisResponse response = client.analyze(request(requestId, userId));

        assertEquals(requestId, response.getRequestId());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("STABLE", response.getFinancialState());
        JsonNode json = new ObjectMapper().readTree(requestBody.get());
        assertEquals(requestId.toString(), json.get("requestId").asText());
        assertEquals("2026-04-01", json.get("statementPeriod").get("from").asText());
    }

    @Test
    void analyzeThrowsAiServiceExceptionOnHttpError() throws Exception {
        startServer(500, "{\"message\":\"error\"}", new AtomicReference<>());
        AiAnalysisClient client = new AiAnalysisClient(webClient());

        assertThrows(AiServiceException.class, () -> client.analyze(request(UUID.randomUUID(), UUID.randomUUID())));
    }

    private void startServer(int statusCode, String responseBody, AtomicReference<String> requestBody) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/analysis", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
    }

    private WebClient webClient() {
        return new AiServiceClientConfig()
                .aiWebClient("http://localhost:" + server.getAddress().getPort());
    }

    private AiAnalysisRequest request(UUID requestId, UUID userId) {
        return AiAnalysisRequest.builder()
                .requestId(requestId)
                .userId(userId)
                .period("2026-04")
                .bank("SBERBANK")
                .statementIds(List.of(UUID.randomUUID()))
                .statementPeriod(AiStatementPeriodDto.builder()
                        .from(LocalDate.of(2026, 4, 1))
                        .to(LocalDate.of(2026, 4, 30))
                        .build())
                .statementSummary(AiStatementSummaryDto.builder()
                        .openingBalance(BigDecimal.ZERO)
                        .totalIncome(new BigDecimal("100.00"))
                        .totalExpense(new BigDecimal("50.00"))
                        .closingBalance(new BigDecimal("50.00"))
                        .build())
                .transactions(List.of())
                .build();
    }

    private String responseJson(UUID requestId, UUID userId) {
        return """
                {
                  "requestId": "%s",
                  "userId": "%s",
                  "period": "2026-04",
                  "status": "SUCCESS",
                  "summary": null,
                  "financialState": "STABLE",
                  "categoryAnalytics": [],
                  "topCategories": [],
                  "insights": [],
                  "recommendations": [],
                  "message": null
                }
                """.formatted(requestId, userId);
    }
}
