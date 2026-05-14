package com.finsightai.web.model;

import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Transaction {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "statement_id", nullable = false)
    private Statement statement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BankType bank;

    @Column(nullable = false, length = 7)
    private String period;

    @Column(name = "operation_date", nullable = false)
    private LocalDate date;

    @Column(name = "operation_time", nullable = false)
    private LocalTime time;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 255)
    private String category;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (time == null) {
            time = LocalTime.MIDNIGHT;
        }

        if (category == null || category.isBlank()) {
            category = "Прочее";
        }

        if (description == null || description.isBlank()) {
            description = "Не указано";
        }
    }
}
