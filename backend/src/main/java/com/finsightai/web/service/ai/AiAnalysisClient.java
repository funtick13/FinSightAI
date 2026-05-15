package com.finsightai.web.service.ai;

import com.finsightai.web.dto.ai.AiAnalysisRequest;
import com.finsightai.web.dto.ai.AiAnalysisResponse;
import com.finsightai.web.exception.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisClient {

    private final WebClient aiWebClient;

    public AiAnalysisResponse analyze(AiAnalysisRequest request) {
        try {
            log.info(
                    "Sending AI analysis request: requestId={}, userId={}, period={}, bank={}, transactions={}, statementIds={}",
                    request.getRequestId(),
                    request.getUserId(),
                    request.getPeriod(),
                    request.getBank(),
                    request.getTransactions() == null ? 0 : request.getTransactions().size(),
                    request.getStatementIds() == null ? 0 : request.getStatementIds().size()
            );

            AiAnalysisResponse response = aiWebClient.post()
                    .uri("/analysis")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(body -> new AiServiceException(
                                    "AI-service вернул HTTP " + clientResponse.statusCode().value()
                            )))
                    .bodyToMono(AiAnalysisResponse.class)
                    .block();

            if (response == null) {
                throw new AiServiceException("AI-service вернул пустой ответ");
            }

            log.info(
                    "AI analysis response: requestId={}, status={}, financialState={}, recommendations={}",
                    response.getRequestId(),
                    response.getStatus(),
                    response.getFinancialState(),
                    response.getRecommendations() == null ? 0 : response.getRecommendations().size()
            );

            return response;
        } catch (AiServiceException exception) {
            throw exception;
        } catch (WebClientResponseException exception) {
            throw new AiServiceException("AI-service вернул ошибку HTTP " + exception.getStatusCode().value(), exception);
        } catch (RuntimeException exception) {
            throw new AiServiceException("Не удалось вызвать AI-service", exception);
        }
    }
}
