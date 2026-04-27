package com.upkdev.financialtracker.domain.income.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class IncomeResponse {
    private Long id;
    private Long memberId;
    private Long accountId;
    private Long statementId;
    private String sourceName;
    private String institutionName;
    private BigDecimal amount;
    private String incomeCategoryCode;
    private LocalDate incomeDate;
    private String description;
    private LocalDateTime createdAt;
}
