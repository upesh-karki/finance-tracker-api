package com.upkdev.financialtracker.domain.account.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FinancialInstitutionResponse {
    private String code;
    private String name;
    private String country;
}
