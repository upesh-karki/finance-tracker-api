package com.upkdev.financialtracker.domain.account.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FinancialAccountResponse {
    private Long id;
    private Long memberId;
    private String nickname;
    private String institutionName;
    private String accountTypeCode;
    private LocalDate openedDate;
    private LocalDate trackingStartDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private List<MissingStatementMonth> missingMonths;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MissingStatementMonth {
        private int year;
        private int month;
        private String monthLabel;
    }
}
