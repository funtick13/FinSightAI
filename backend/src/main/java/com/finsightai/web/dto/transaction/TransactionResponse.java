package com.finsightai.web.dto.transaction;

import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TransactionResponse {
    private UUID id;
    private UUID statementId;
    private BankType bank;
    private String period;
    private LocalDate date;
    private LocalTime time;
    private TransactionType type;
    private BigDecimal amount;
    private String category;
    private String description;
}
