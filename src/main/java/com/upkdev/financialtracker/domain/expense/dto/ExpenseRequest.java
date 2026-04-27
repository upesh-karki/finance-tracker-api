package com.upkdev.financialtracker.domain.expense.dto;

import com.upkdev.financialtracker.domain.expense.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    private Long accountId;
    private Long statementId;
    private String institutionName;

    @NotBlank(message = "Expense name is required")
    private String expenseName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    private ExpenseCategory category;
    private String description;
    private LocalDate expenseDate;
}
