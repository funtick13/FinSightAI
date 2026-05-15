package com.finsightai.web.dto.ai;

import com.finsightai.web.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AiTransactionDto {
    private UUID id;
    private LocalDate date;
    private String time;
    private TransactionType type;
    private BigDecimal amount;
    private String category;
    private String description;
}
