package com.upkdev.financialtracker.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VerifyOtpRequest {
    @NotBlank private String email;
    @NotBlank private String otpCode;
}
