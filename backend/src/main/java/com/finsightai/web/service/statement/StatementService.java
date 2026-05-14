package com.finsightai.web.service.statement;

import com.finsightai.web.dto.statement.StatementResponse;
import com.finsightai.web.dto.statement.StoredFile;
import com.finsightai.web.exception.InvalidStatementException;
import com.finsightai.web.exception.StatementNotFoundException;
import com.finsightai.web.exception.UnsupportedFileTypeException;
import com.finsightai.web.mapper.StatementMapper;
import com.finsightai.web.model.Statement;
import com.finsightai.web.model.User;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.StatementStatus;
import com.finsightai.web.repository.StatementRepository;
import com.finsightai.web.service.statement.processing.StatementProcessingService;
import com.finsightai.web.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatementService {
    private final StatementRepository statementRepository;
    private final LocalFileStorageService fileStorageService;
    private final StatementMapper statementMapper;
    private final StatementProcessingService statementProcessingService;
    private final TransactionService transactionService;

    public StatementResponse uploadStatement(
            User user,
            BankType bank,
            String period,
            MultipartFile file
    ) {

        validateUpload(bank, period, file);

        if (!"application/pdf".equals(file.getContentType())) {
            throw new UnsupportedFileTypeException();
        }

        UUID statementId = UUID.randomUUID();

        StoredFile storedFile = fileStorageService.save(
                file,
                user.getId(),
                statementId
        );

        LocalDateTime now = LocalDateTime.now();

        Statement statement = Statement.builder()
                .user(user)
                .bank(bank)
                .period(period)
                .originalFileName(storedFile.getOriginalFileName())
                .storedFileName(storedFile.getStoredFileName())
                .filePath(storedFile.getFilePath())
                .contentType(storedFile.getContentType())
                .fileSize(storedFile.getFileSize())
                .status(StatementStatus.UPLOADED)
                .uploadedAt(now)
                .updatedAt(now)
                .processedAt(null)
                .build();

        Statement savedStatement = statementRepository.save(statement);
        statementProcessingService.processStatement(savedStatement.getId());

        Statement processedStatement = statementRepository.findById(savedStatement.getId())
                .orElse(savedStatement);

        return statementMapper.toResponse(processedStatement);
    }

    public Page<StatementResponse> getUserStatements(UUID userId, Pageable pageable) {
        return statementRepository.findAllByUserId(userId, pageable)
                .map(statementMapper::toResponse);
    }

    public StatementResponse getStatement(UUID userId, UUID statementId) {
        Statement statement = statementRepository.findByIdAndUserId(statementId, userId)
                .orElseThrow(StatementNotFoundException::new);
        return statementMapper.toResponse(statement);
    }

    public void deleteStatement(UUID userId, UUID statementId) {
        Statement statement = statementRepository.findByIdAndUserId(statementId, userId)
                .orElseThrow(StatementNotFoundException::new);

        transactionService.deleteByStatementId(statementId);
        fileStorageService.delete(statement.getFilePath());
        statementRepository.delete(statement);
    }

    private void validateUpload(BankType bank, String period, MultipartFile file) {
        if (bank == null) {
            throw new InvalidStatementException("Банк не указан");
        }

        if (period == null || period.isBlank()) {
            throw new InvalidStatementException("Расчётный период не указан");
        }

        if (!period.matches("\\d{4}-\\d{2}")) {
            throw new InvalidStatementException("Период должен быть в формате YYYY-MM");
        }

        if (file == null || file.isEmpty()) {
            throw new InvalidStatementException("Файл выписки не передан");
        }

        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new UnsupportedFileTypeException("Можно загружать только PDF-файлы");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new UnsupportedFileTypeException("Файл должен иметь расширение .pdf");
        }
    }
}
