package com.finsightai.web.service.statement.normalizer;

import com.finsightai.web.dto.transaction.ParsedTransaction;
import com.finsightai.web.dto.transaction.TransactionCandidate;
import com.finsightai.web.exception.StatementParsingException;
import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Component
public class TransactionNormalizer {

    private static final String DEFAULT_CATEGORY = "Прочее";
    private static final String DEFAULT_DESCRIPTION = "Не указано";

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d.M.uuuu"),
            DateTimeFormatter.ofPattern("d/M/uuuu"),
            DateTimeFormatter.ofPattern("d-M-uuuu")
    );

    private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("H:mm[:ss]"),
            DateTimeFormatter.ISO_LOCAL_TIME
    );

    public TransactionCandidate normalize(
            ParsedTransaction parsedTransaction,
            UUID userId,
            UUID statementId,
            BankType bank,
            String period
    ) {
        if (parsedTransaction == null) {
            throw new StatementParsingException("Операция выписки не передана для нормализации");
        }

        LocalDate date = parseDate(parsedTransaction.getRawDate());
        LocalTime time = parseTime(parsedTransaction.getRawTime());
        NormalizedAmount normalizedAmount = parseAmount(parsedTransaction.getRawAmount());

        return new TransactionCandidate(
                date,
                time,
                normalizedAmount.amount(),
                normalizedAmount.type(),
                valueOrDefault(parsedTransaction.getRawCategory(), DEFAULT_CATEGORY),
                valueOrDefault(parsedTransaction.getRawDescription(), DEFAULT_DESCRIPTION),
                bank,
                period,
                userId,
                statementId
        );
    }

    private LocalDate parseDate(String rawDate) {
        String normalized = normalizeSpaces(rawDate);
        if (normalized.isBlank()) {
            throw new StatementParsingException("Дата операции не указана");
        }

        normalized = expandTwoDigitYear(normalized);

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(normalized, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new StatementParsingException("Дата операции не распознана");
    }

    private String expandTwoDigitYear(String value) {
        if (!value.matches("\\d{1,2}[./-]\\d{1,2}[./-]\\d{2}")) {
            return value;
        }

        int delimiterIndex = Math.max(value.lastIndexOf('.'), Math.max(value.lastIndexOf('/'), value.lastIndexOf('-')));
        return value.substring(0, delimiterIndex + 1) + "20" + value.substring(delimiterIndex + 1);
    }

    private LocalTime parseTime(String rawTime) {
        String normalized = normalizeSpaces(rawTime);
        if (normalized.isBlank()) {
            return LocalTime.MIDNIGHT;
        }

        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                return LocalTime.parse(normalized, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next supported bank time format.
            }
        }

        throw new StatementParsingException("Время операции не распознано");
    }

    private NormalizedAmount parseAmount(String rawAmount) {
        String normalized = normalizeSpaces(rawAmount).replace('−', '-');
        if (normalized.isBlank()) {
            throw new StatementParsingException("Сумма операции не указана");
        }

        boolean income = normalized.startsWith("+");
        String numeric = normalized
                .replaceAll("(?i)(руб\\.?|RUB|₽)", "")
                .replace(" ", "")
                .replace("\u00A0", "")
                .replace("+", "")
                .replace("-", "")
                .replace(",", ".");

        try {
            BigDecimal absoluteAmount = new BigDecimal(numeric).abs().setScale(2, RoundingMode.HALF_UP);
            TransactionType type = income ? TransactionType.INCOME : TransactionType.EXPENSE;
            BigDecimal amount = income ? absoluteAmount : absoluteAmount.negate();
            return new NormalizedAmount(amount, type);
        } catch (NumberFormatException exception) {
            throw new StatementParsingException("Сумма операции не распознана", exception);
        }
    }

    private String valueOrDefault(String value, String defaultValue) {
        String normalized = normalizeSpaces(value);
        return normalized.isBlank() ? defaultValue : normalized;
    }

    private String normalizeSpaces(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    private record NormalizedAmount(BigDecimal amount, TransactionType type) {
    }
}
