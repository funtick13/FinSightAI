package com.finsightai.web.service.statement.processing;

import com.finsightai.web.dto.statement.ParsedTransaction;
import com.finsightai.web.dto.statement.TransactionCandidate;
import com.finsightai.web.exception.StatementNotFoundException;
import com.finsightai.web.exception.StatementParsingException;
import com.finsightai.web.model.Statement;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.StatementStatus;
import com.finsightai.web.repository.StatementRepository;
import com.finsightai.web.service.statement.duplicate.DuplicateTransactionDetector;
import com.finsightai.web.service.statement.normalizer.TransactionNormalizer;
import com.finsightai.web.service.statement.parser.BankStatementParser;
import com.finsightai.web.service.statement.parser.PdfTextExtractor;
import com.finsightai.web.service.statement.parser.SberbankStatementParser;
import com.finsightai.web.service.ai.FinancialAnalysisService;
import com.finsightai.web.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementProcessingServiceImpl implements StatementProcessingService {

    private final StatementRepository statementRepository;
    private final PdfTextExtractor pdfTextExtractor;
    private final SberbankStatementParser sberbankStatementParser;
    private final TransactionNormalizer transactionNormalizer;
    private final DuplicateTransactionDetector duplicateTransactionDetector;
    private final TransactionService transactionService;
    private final FinancialAnalysisService financialAnalysisService;

    @Override
    @Transactional
    public void processStatement(UUID statementId) {
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(StatementNotFoundException::new);

        markProcessing(statement);

        try {
            List<TransactionCandidate> uniqueCandidates = buildCandidates(statement);

            if (uniqueCandidates.isEmpty()) {
                throw new StatementParsingException("В выписке не найдено корректных операций");
            }

            transactionService.saveCandidates(uniqueCandidates);
            analyzeStatementPeriod(statement);

            markProcessed(statement);

            log.info(
                    "Выписка {} успешно обработана и операции сохранены: unique={}",
                    statementId,
                    uniqueCandidates.size()
            );
        } catch (RuntimeException exception) {
            logFailure(statementId, exception);
            markFailed(statement, exception);
        }
    }

    private void analyzeStatementPeriod(Statement statement) {
        try {
            financialAnalysisService.analyzePeriod(
                    statement.getUser().getId(),
                    statement.getPeriod(),
                    statement.getBank()
            );
        } catch (RuntimeException exception) {
            log.warn(
                    "AI-анализ не выполнен: statementId={}, reason={}",
                    statement.getId(),
                    exception.getMessage()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionCandidate> processStatementToCandidates(UUID statementId) {
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(StatementNotFoundException::new);

        return buildCandidates(statement);
    }

    private List<TransactionCandidate> buildCandidates(Statement statement) {
        UUID statementId = statement.getId();

        String rawText = pdfTextExtractor.extract(Path.of(statement.getFilePath()));

        if (log.isDebugEnabled()) {
            log.debug("Выделенный PDF текст выписки {}:\n{}", statementId, sanitizeForDebug(rawText));
        }

        BankStatementParser parser = resolveParser(statement.getBank());

        List<ParsedTransaction> parsedTransactions = parser.parse(rawText);
        List<TransactionCandidate> candidates = normalizeTransactions(statement, parsedTransactions);
        List<TransactionCandidate> uniqueCandidates = duplicateTransactionDetector.removeDuplicates(candidates);

        if (uniqueCandidates.isEmpty()) {
            throw new StatementParsingException("В выписке не найдено корректных операций");
        }

        log.info(
                "Выписка {} разобрана: parsed={}, normalized={}, unique={}",
                statementId,
                parsedTransactions.size(),
                candidates.size(),
                uniqueCandidates.size()
        );

        return uniqueCandidates;
    }

    private void logFailure(UUID statementId, RuntimeException exception) {
        if (exception instanceof StatementParsingException) {
            log.warn("Ошибка в обработке выписки: statementId={}, reason={}", statementId, exception.getMessage());
            return;
        }

        log.error("Ошибка в обработке выписки: statementId={}", statementId, exception);
    }

    private BankStatementParser resolveParser(BankType bank) {
        if (bank == BankType.SBERBANK) {
            return sberbankStatementParser;
        }

        throw new StatementParsingException("Банк выписки не поддерживается парсером");
    }

    private List<TransactionCandidate> normalizeTransactions(
            Statement statement,
            List<ParsedTransaction> parsedTransactions
    ) {
        List<TransactionCandidate> candidates = new ArrayList<>();

        for (ParsedTransaction parsedTransaction : parsedTransactions) {
            try {
                candidates.add(transactionNormalizer.normalize(
                        parsedTransaction,
                        statement.getUser().getId(),
                        statement.getId(),
                        statement.getBank(),
                        statement.getPeriod()
                ));
            } catch (StatementParsingException exception) {
                log.warn("Пропуск невалидных транзакций {}", statement.getId(), exception);
            }
        }

        return candidates;
    }

    private void markProcessing(Statement statement) {
        LocalDateTime now = LocalDateTime.now();
        statement.setStatus(StatementStatus.PROCESSING);
        statement.setUpdatedAt(now);
        statement.setProcessedAt(null);
        statement.setErrorMessage(null);
        statementRepository.save(statement);
    }

    private void markProcessed(Statement statement) {
        LocalDateTime now = LocalDateTime.now();
        statement.setStatus(StatementStatus.PROCESSED);
        statement.setUpdatedAt(now);
        statement.setProcessedAt(now);
        statement.setErrorMessage(null);
        statementRepository.save(statement);
    }

    private void markFailed(Statement statement, RuntimeException exception) {
        LocalDateTime now = LocalDateTime.now();
        statement.setStatus(StatementStatus.FAILED);
        statement.setUpdatedAt(now);
        statement.setProcessedAt(now);
        statement.setErrorMessage(trimErrorMessage(exception.getMessage()));
        statementRepository.save(statement);
    }

    private String trimErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Не удалось обработать выписку";
        }

        return message.length() <= 500 ? message : message.substring(0, 500);
    }

    private String sanitizeForDebug(String rawText) {
        if (rawText == null) {
            return "";
        }

        return rawText
                .replaceAll("(?m)(Владелец счёта)\\R[^\\R]+", "$1\n[hidden]")
                .replaceAll("(?m)(Номер счёта)\\s+[\\d\\s]+", "$1 [hidden]")
                .replaceAll("(?m)(Карта)\\s+.+", "$1 [hidden]")
                .replaceAll("\\*{4}\\d{4}", "****0000")
                .replaceAll("\\b\\d{12,}\\b", "[number]");
    }
}
