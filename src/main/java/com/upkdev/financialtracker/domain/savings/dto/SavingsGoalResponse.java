package com.upkdev.financialtracker.domain.savings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsGoalResponse {
    private Long id;
    private Long memberId;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal monthlySavingsTarget;
    private BigDecimal income;
    private LocalDate targetDate;
    private String status;
    private LocalDateTime createdAt;
}
