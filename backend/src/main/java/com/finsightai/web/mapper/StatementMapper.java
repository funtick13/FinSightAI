package com.finsightai.web.mapper;

import com.finsightai.web.dto.statement.StatementResponse;
import com.finsightai.web.model.Statement;
import org.springframework.stereotype.Component;

@Component
public class StatementMapper {

    public StatementResponse toResponse(Statement statement) {
        return StatementResponse.builder()
                .id(statement.getId())
                .bank(statement.getBank())
                .period(statement.getPeriod())
                .originalFileName(statement.getOriginalFileName())
                .fileSize(statement.getFileSize())
                .status(statement.getStatus())
                .uploadedAt(statement.getUploadedAt())
                .processedAt(statement.getProcessedAt())
                .errorMessage(statement.getErrorMessage())
                .build();
    }
}