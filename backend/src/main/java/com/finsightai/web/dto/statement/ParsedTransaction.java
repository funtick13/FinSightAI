package com.finsightai.web.dto.statement;

public record ParsedTransaction(
        String rawDate,
        String rawTime,
        String rawCategory,
        String rawDescription,
        String rawAmount,
        String rawLine
) {
}
