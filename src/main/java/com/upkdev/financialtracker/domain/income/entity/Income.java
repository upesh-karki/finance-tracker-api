package com.upkdev.financialtracker.domain.income.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "income", schema = "ods")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "statement_id")
    private Long statementId;

    @Column(name = "source_name", nullable = false)
    private String sourceName;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "income_category_code", nullable = false)
    private String incomeCategoryCode;

    @Column(name = "income_date", nullable = false)
    private LocalDate incomeDate;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
