package com.finsightai.web.service.statement.parser;

import com.finsightai.web.dto.transaction.ParsedTransaction;

import java.util.List;

public interface BankStatementParser {
    List<ParsedTransaction> parse(String rawText);
}
