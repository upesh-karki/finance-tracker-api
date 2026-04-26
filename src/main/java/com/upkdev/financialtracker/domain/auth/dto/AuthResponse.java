package com.upkdev.financialtracker.domain.auth.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String token;
    private Long memberId;
    private String firstName;
    private String lastName;
    private String email;
    private boolean emailVerified;
    private String authProvider;
    // null token means email verification pending
}
