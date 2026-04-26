package com.upkdev.financialtracker.domain.statement.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StatementUploadResponse {
    private int transactionCount;
    private int expenseCount;
    private int incomeCount;
    private int creditCardPaymentCount;
    private List<ExtractedTransaction> expenses;
    private List<ExtractedTransaction> income;
    private List<ExtractedTransaction> creditCardPayments;
    private String rawTextPreview;
    private String modelUsed;
}
