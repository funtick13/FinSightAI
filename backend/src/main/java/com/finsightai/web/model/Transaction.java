package com.finsightai.web.model;

import com.finsightai.web.model.enums.BankType;
import com.finsightai.web.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_transactions_user_period", columnList = "user_id, period"),
                @Index(name = "idx_transactions_statement", columnList = "statement_id"),
                @Index(name = "idx_transactions_type", columnList = "type"),
                @Index(name = "idx_transactions_category", columnList = "category")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Transaction {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "statement_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
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