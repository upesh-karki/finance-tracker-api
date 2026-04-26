package com.upkdev.financialtracker.domain.account.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountStatementResponse {
    private Long id;
    private Long accountId;
    private Integer statementYear;
    private Integer statementMonth;
    private String status;
    private String fileName;
    private Integer transactionCount;
    private LocalDateTime uploadedAt;
}
