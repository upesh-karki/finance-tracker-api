package com.upkdev.financialtracker.domain.member.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String occupation;
    private String phoneNumber;
    private String profileStatus;
    private LocalDateTime createdAt;
}
