package com.upkdev.financialtracker.domain.savings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SavingsGoalRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotBlank(message = "Goal name is required")
    private String goalName;

    private BigDecimal targetAmount;
    private BigDecimal income;
    private BigDecimal monthlySavingsTarget;
    private LocalDate targetDate;
}
