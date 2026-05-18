package com.finsightai.web.mapper;

import com.finsightai.web.dto.ai.AiTransactionDto;
import com.finsightai.web.model.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Component
public class AiTransactionMapper {

    private static final DateTimeFormatter AI_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String DEFAULT_CATEGORY = "Прочее";
    private static final String DEFAULT_DESCRIPTION = "Не указано";

    public AiTransactionDto toDto(Transaction transaction) {
        return AiTransactionDto.builder()
                .id(transaction.getId())
                .date(transaction.getDate())
                .time(formatTime(transaction))
                .type(transaction.getType())
                .amount(toPositiveAmount(transaction.getAmount()))
                .category(category(transaction))
                .description(description(transaction))
                .build();
    }

    private String formatTime(Transaction transaction) {
        if (transaction.getTime() == null) {
            return null;
        }

        return transaction.getTime().format(AI_TIME_FORMATTER);
    }

    private BigDecimal toPositiveAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }

        return amount.abs();
    }

    private String category(Transaction transaction) {
        String category = clean(transaction.getCategory());
        return category == null ? DEFAULT_CATEGORY : category;
    }

    private String description(Transaction transaction) {
        String description = clean(transaction.getDescription());
        return description == null ? DEFAULT_DESCRIPTION : description;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
        return normalized.isBlank() ? null : normalized;
    }
}