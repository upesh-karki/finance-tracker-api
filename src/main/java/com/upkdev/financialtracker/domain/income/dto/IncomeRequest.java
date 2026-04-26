package com.upkdev.financialtracker.domain.income.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class IncomeRequest {

    @NotNull
    private Long memberId;

    private Long accountId;
    private Long statementId;

    @NotBlank
    private String sourceName;

    @NotNull @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    private String incomeCategoryCode;

    @NotNull
    private LocalDate incomeDate;

    private String description;
}
