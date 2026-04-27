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
    private int investmentCount;
    // Flat unified list — used by the new single-table UI
    private List<ExtractedTransaction> transactions;
    // Investments tracked separately — not counted as expense or income
    private List<ExtractedTransaction> investments;
    // Legacy split lists (kept for backward compat)
    private List<ExtractedTransaction> expenses;
    private List<ExtractedTransaction> income;
    private List<ExtractedTransaction> transfers;
    private List<ExtractedTransaction> creditCardPayments;
    private String rawTextPreview;
    private String modelUsed;
}
