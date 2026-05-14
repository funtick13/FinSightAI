package com.finsightai.web.service.statement.processing;

import com.finsightai.web.dto.statement.ParsedTransaction;
import com.finsightai.web.dto.statement.TransactionCandidate;
import com.finsightai.web.exception.StatementParsingException;
import com.finsightai.web.model.Statement;
import com.finsightai.web.model.User;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.StatementStatus;
import com.finsightai.web.model.enums.TransactionType;
import com.finsightai.web.repository.StatementRepository;
import com.finsightai.web.service.statement.duplicate.DuplicateTransactionDetector;
import com.finsightai.web.service.statement.normalizer.TransactionNormalizer;
import com.finsightai.web.service.statement.parser.PdfTextExtractor;
import com.finsightai.web.service.statement.parser.SberbankStatementParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatementProcessingServiceImplTest {

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private PdfTextExtractor pdfTextExtractor;

    @Mock
    private SberbankStatementParser sberbankStatementParser;

    @Mock
    private TransactionNormalizer transactionNormalizer;

    @Mock
    private DuplicateTransactionDetector duplicateTransactionDetector;

    @InjectMocks
    private StatementProcessingServiceImpl processingService;

    @Test
    void processStatementMarksStatementProcessedOnSuccess() {
        Statement statement = statement();
        ParsedTransaction parsedTransaction = parsedTransaction();
        TransactionCandidate candidate = candidate(statement);

        when(statementRepository.findById(statement.getId())).thenReturn(Optional.of(statement));
        when(pdfTextExtractor.extract(Path.of(statement.getFilePath()))).thenReturn("raw text");
        when(sberbankStatementParser.parse("raw text")).thenReturn(List.of(parsedTransaction));
        when(transactionNormalizer.normalize(
                parsedTransaction,
                statement.getUser().getId(),
                statement.getId(),
                statement.getBank(),
                statement.getPeriod()
        )).thenReturn(candidate);
        when(duplicateTransactionDetector.removeDuplicates(List.of(candidate))).thenReturn(List.of(candidate));

        List<TransactionCandidate> result = processingService.processStatementToCandidates(statement.getId());

        assertEquals(List.of(candidate), result);
        assertEquals(StatementStatus.PROCESSED, statement.getStatus());
        assertNotNull(statement.getProcessedAt());
        assertNull(statement.getErrorMessage());
        verify(statementRepository, atLeastOnce()).save(statement);
    }

    @Test
    void processStatementMarksStatementFailedWhenFileCannotBeRead() {
        Statement statement = statement();

        when(statementRepository.findById(statement.getId())).thenReturn(Optional.of(statement));
        when(pdfTextExtractor.extract(Path.of(statement.getFilePath())))
                .thenThrow(new StatementParsingException("Файл выписки не найден"));

        processingService.processStatement(statement.getId());

        assertEquals(StatementStatus.FAILED, statement.getStatus());
        assertEquals("Файл выписки не найден", statement.getErrorMessage());
        assertNotNull(statement.getProcessedAt());
        verify(sberbankStatementParser, never()).parse(any());
    }

    @Test
    void processStatementMarksStatementFailedWhenTransactionsAreNotFound() {
        Statement statement = statement();

        when(statementRepository.findById(statement.getId())).thenReturn(Optional.of(statement));
        when(pdfTextExtractor.extract(Path.of(statement.getFilePath()))).thenReturn("raw text");
        when(sberbankStatementParser.parse("raw text"))
                .thenThrow(new StatementParsingException("Операции в выписке не найдены"));

        processingService.processStatement(statement.getId());

        assertEquals(StatementStatus.FAILED, statement.getStatus());
        assertEquals("Операции в выписке не найдены", statement.getErrorMessage());
        assertNotNull(statement.getProcessedAt());
        verify(transactionNormalizer, never()).normalize(any(), any(), any(), any(), any());
    }

    private Statement statement() {
        UUID statementId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        return Statement.builder()
                .id(statementId)
                .user(user)
                .bank(BankType.SBERBANK)
                .period("2026-04")
                .originalFileName("statement.pdf")
                .storedFileName("statement.pdf")
                .filePath("uploads/statements/" + userId + "/" + statementId + ".pdf")
                .contentType("application/pdf")
                .fileSize(100L)
                .status(StatementStatus.UPLOADED)
                .uploadedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ParsedTransaction parsedTransaction() {
        return new ParsedTransaction(
                "01.04.2026",
                "12:30",
                "Супермаркеты",
                "Покупка",
                "100,00 ₽",
                null
        );
    }

    private TransactionCandidate candidate(Statement statement) {
        return new TransactionCandidate(
                LocalDate.of(2026, 4, 1),
                LocalTime.of(12, 30),
                new BigDecimal("-100.00"),
                TransactionType.EXPENSE,
                "Супермаркеты",
                "Покупка",
                statement.getBank(),
                statement.getPeriod(),
                statement.getUser().getId(),
                statement.getId()
        );
    }
}
