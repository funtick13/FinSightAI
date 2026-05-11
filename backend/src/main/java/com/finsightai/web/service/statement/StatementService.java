package com.finsightai.web.service.statement;

import com.finsightai.web.dto.statement.StatementResponse;
import com.finsightai.web.dto.statement.StoredFile;
import com.finsightai.web.mapper.StatementMapper;
import com.finsightai.web.model.Statement;
import com.finsightai.web.model.User;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.StatementStatus;
import com.finsightai.web.repository.StatementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatementService {
    private final StatementRepository statementRepository;
    private final LocalFileStorageService fileStorageService;
    private final StatementMapper statementMapper;

    public StatementResponse uploadStatement(
            User user,
            BankType bank,
            String period,
            MultipartFile file
    ) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        if (bank == null) {
            throw new IllegalArgumentException("Банк не указан");
        }

        if (period == null || period.isBlank()) {
            throw new IllegalArgumentException("Расчётный период не указан");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл выписки не передан");
        }

        if (!period.matches("\\d{4}-\\d{2}")) {
            throw new IllegalArgumentException("Период должен быть в формате YYYY-MM");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Можно загружать только PDF-файлы");
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
                .processedAt(now)
                .build();

        Statement savedStatement = statementRepository.save(statement);

        return statementMapper.toResponse(savedStatement);
    }

    public Page<StatementResponse> getUserStatements(UUID userId, Pageable pageable) {
        return statementRepository.findAllByUserId(userId, pageable)
                .map(statementMapper::toResponse);
    }

    public StatementResponse getStatement(UUID userId, UUID statementId) {
        Statement statement = statementRepository.findByIdAndUserId(statementId, userId)
                .orElseThrow(() -> new RuntimeException("Выписка не найдена"));
        return statementMapper.toResponse(statement);
    }

    public void deleteStatement(UUID userId, UUID statementId) {
        Statement statement = statementRepository.findByIdAndUserId(statementId, userId)
                .orElseThrow(() -> new RuntimeException("Выписка не найдена"));

        fileStorageService.delete(statement.getFilePath());
        statementRepository.delete(statement);
    }
}
