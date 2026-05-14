package com.finsightai.web.dto.statement;

import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionCandidate {
    private LocalDate date;
    private LocalTime time;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private String description;
    private BankType bank;
    private String period;
    private UUID userId;
    private UUID statementId;
}
