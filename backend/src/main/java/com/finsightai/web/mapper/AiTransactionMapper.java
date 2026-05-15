package com.finsightai.web.mapper;

import com.finsightai.web.dto.ai.AiTransactionDto;
import com.finsightai.web.model.Transaction;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class AiTransactionMapper {
    private static final DateTimeFormatter AI_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String DEFAULT_CATEGORY = "Прочее";

    public AiTransactionDto toDto(Transaction transaction) {
        return AiTransactionDto.builder()
                .id(transaction.getId())
                .date(transaction.getDate())
                .time(formatTime(transaction))
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .category(category(transaction))
                .description(clean(transaction.getDescription()))
                .build();
    }

    private String formatTime(Transaction transaction) {
        if (transaction.getTime() == null) {
            return null;
        }

        return transaction.getTime().format(AI_TIME_FORMATTER);
    }

    private String category(Transaction transaction) {
        String category = clean(transaction.getCategory());
        return category == null ? DEFAULT_CATEGORY : category;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
        return normalized.isBlank() ? null : normalized;
    }
}
