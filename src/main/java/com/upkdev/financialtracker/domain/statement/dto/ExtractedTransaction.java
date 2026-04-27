package com.upkdev.financialtracker.domain.statement.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ExtractedTransaction {
    private String date;
    private String description;
    private BigDecimal amount;
    private String type; // DEBIT or CREDIT
    private String suggestedCategory;
    private Boolean isCreditCardPayment;
    private Boolean isTransfer;
    private String transactionType; // EXPENSE, INCOME, INVESTMENT, TRANSFER, CC_PAYMENT
    // Computed display type for unified UI: EXPENSE | INCOME | NEUTRAL | INVESTMENT
    private String displayType;
}
