package com.upkdev.financialtracker.domain.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FinancialAccountRequest {

    @NotNull
    private Long memberId;

    @NotBlank
    private String nickname;

    @NotBlank
    private String institutionName;

    @NotBlank
    private String accountTypeCode;

    private LocalDate openedDate;
}
