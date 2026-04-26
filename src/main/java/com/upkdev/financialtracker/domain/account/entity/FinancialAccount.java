package com.upkdev.financialtracker.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_account", schema = "ods")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FinancialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "institution_name", nullable = false)
    private String institutionName;

    @Column(name = "account_type_code", nullable = false)
    private String accountTypeCode;

    @Column(name = "opened_date")
    private LocalDate openedDate;

    @Column(name = "tracking_start_date", nullable = false)
    private LocalDate trackingStartDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
