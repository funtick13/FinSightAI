package com.finsightai.web.service.statement.parser;

import com.finsightai.web.dto.statement.ParsedTransaction;
import com.finsightai.web.exception.StatementParsingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SberbankStatementParser implements BankStatementParser {

    private static final String DEFAULT_CATEGORY = "Прочее";
    private static final String DEFAULT_DESCRIPTION = "Не указано";

    private static final String MONEY = "[+\\-−]?\\s*\\d[\\d\\s\\u00A0]*[,.]\\d{2}";

    private static final Pattern SBER_OPERATION_LINE = Pattern.compile(
            "^(?<date>\\d{2}\\.\\d{2}\\.\\d{4})\\s+"
                    + "(?<time>\\d{2}:\\d{2})\\s+"
                    + "(?<category>.+?)\\s+"
                    + "(?<amount>" + MONEY + ")\\s+"
                    + "(?<balance>" + MONEY + ")$",
            Pattern.UNICODE_CASE
    );

    private static final Pattern DESCRIPTION_START_LINE = Pattern.compile(
            "^\\d{2}\\.\\d{2}\\.\\d{4}\\s+\\d{4,8}\\s+.+$",
            Pattern.UNICODE_CASE
    );

    private static final Pattern LEGACY_TRANSACTION_LINE = Pattern.compile(
            "^(?<date>\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}|\\d{4}-\\d{2}-\\d{2})\\s+"
                    + "(?:(?<time>\\d{1,2}:\\d{2}(?::\\d{2})?)\\s+)?"
                    + "(?<body>.+?)\\s+"
                    + "(?<amount>" + MONEY + "\\s*(?:₽|руб\\.?|RUB)?)\\s*$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    @Override
    public List<ParsedTransaction> parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new StatementParsingException("Текст PDF-выписки пустой");
        }

        List<ParsedTransaction> transactions = new ArrayList<>();
        TransactionDraft current = null;

        for (String line : rawText.split("\\R")) {
            String normalizedLine = normalizeLine(line);
            if (normalizedLine.isBlank()) {
                continue;
            }

            Matcher operationMatcher = SBER_OPERATION_LINE.matcher(normalizedLine);
            if (operationMatcher.matches()) {
                current = flushCurrent(transactions, current);
                current = TransactionDraft.from(operationMatcher, normalizedLine);
                continue;
            }

            if (current != null && isDescriptionLine(normalizedLine)) {
                current.descriptionLines().add(normalizedLine);
                continue;
            }

            if (current == null) {
                parseLegacyLine(normalizedLine, transactions);
            }
        }

        flushCurrent(transactions, current);

        if (transactions.isEmpty()) {
            throw new StatementParsingException("Операции в выписке не найдены");
        }

        log.debug("Parsed {} Sberbank statement transaction lines", transactions.size());
        return transactions;
    }

    private TransactionDraft flushCurrent(List<ParsedTransaction> transactions, TransactionDraft current) {
        if (current == null) {
            return null;
        }

        transactions.add(new ParsedTransaction(
                current.rawDate(),
                current.rawTime(),
                valueOrDefault(current.rawCategory(), DEFAULT_CATEGORY),
                cleanDescription(current.descriptionLines()),
                current.rawAmount(),
                current.rawLine()
        ));
        return null;
    }

    private void parseLegacyLine(String normalizedLine, List<ParsedTransaction> transactions) {
        Matcher matcher = LEGACY_TRANSACTION_LINE.matcher(normalizedLine);
        if (!matcher.matches()) {
            return;
        }

        ParsedBody parsedBody = parseLegacyBody(matcher.group("body"));
        transactions.add(new ParsedTransaction(
                matcher.group("date"),
                matcher.group("time"),
                parsedBody.category(),
                parsedBody.description(),
                matcher.group("amount"),
                normalizedLine
        ));
    }

    private boolean isDescriptionLine(String line) {
        return DESCRIPTION_START_LINE.matcher(line).matches()
                || line.startsWith("карте ")
                || line.startsWith("****")
                || line.contains("Операция по карте")
                || line.contains("Операция по");
    }

    private String cleanDescription(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return DEFAULT_DESCRIPTION;
        }

        String description = normalizeSpaces(String.join(" ", lines))
                .replaceFirst("^\\d{2}\\.\\d{2}\\.\\d{4}\\s+\\d{4,8}\\s+", "")
                .replaceAll("\\.?\\s*Операция по\\s+карте\\s*\\*+\\d+", "")
                .replaceAll("\\.?\\s*Операция по карте\\s*\\*+\\d+", "")
                .replaceAll("\\*+\\d+", "");

        description = normalizeSpaces(description)
                .replaceAll("[.\\s]+$", "")
                .trim();

        return description.isBlank() ? DEFAULT_DESCRIPTION : description;
    }

    private ParsedBody parseLegacyBody(String rawBody) {
        String body = normalizeLine(rawBody);
        if (body.isBlank()) {
            return new ParsedBody(DEFAULT_CATEGORY, DEFAULT_DESCRIPTION);
        }

        String[] pipeSeparated = body.split("\\s*\\|\\s*");
        if (pipeSeparated.length >= 2) {
            return new ParsedBody(valueOrDefault(pipeSeparated[0], DEFAULT_CATEGORY), valueOrDefault(joinTail(pipeSeparated), DEFAULT_DESCRIPTION));
        }

        String[] columnSeparated = body.split("\\s{2,}");
        if (columnSeparated.length >= 2) {
            return new ParsedBody(valueOrDefault(columnSeparated[0], DEFAULT_CATEGORY), valueOrDefault(joinTail(columnSeparated), DEFAULT_DESCRIPTION));
        }

        return new ParsedBody(DEFAULT_CATEGORY, normalizeSpaces(body));
    }

    private String normalizeLine(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\u00A0', ' ').trim();
    }

    private String joinTail(String[] values) {
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < values.length; i++) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(values[i]);
        }
        return normalizeSpaces(result.toString());
    }

    private String normalizeSpaces(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    private String valueOrDefault(String value, String defaultValue) {
        String normalized = normalizeSpaces(value);
        return normalized.isBlank() ? defaultValue : normalized;
    }

    private record TransactionDraft(
            String rawDate,
            String rawTime,
            String rawCategory,
            String rawAmount,
            String rawLine,
            List<String> descriptionLines
    ) {
        private static TransactionDraft from(Matcher matcher, String rawLine) {
            return new TransactionDraft(
                    matcher.group("date"),
                    matcher.group("time"),
                    matcher.group("category"),
                    matcher.group("amount"),
                    rawLine,
                    new ArrayList<>()
            );
        }
    }

    private record ParsedBody(String category, String description) {
    }
}
