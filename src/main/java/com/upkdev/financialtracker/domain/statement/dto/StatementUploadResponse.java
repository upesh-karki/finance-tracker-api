package com.upkdev.financialtracker.domain.statement.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StatementUploadResponse {
    private int transactionCount;
    private int expenseCount;
    private int incomeCount;
    private int creditCardPaymentCount;
    private int transferCount;
    private List<ExtractedTransaction> expenses;
    private List<ExtractedTransaction> income;
    private List<ExtractedTransaction> transfers;
    private List<ExtractedTransaction> creditCardPayments;
    private String rawTextPreview;
    private String modelUsed;
}
