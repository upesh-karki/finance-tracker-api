package com.upkdev.financialtracker.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_statement", schema = "ods")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "statement_year", nullable = false)
    private Integer statementYear;

    @Column(name = "statement_month", nullable = false)
    private Integer statementMonth;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "transaction_count")
    private Integer transactionCount = 0;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
