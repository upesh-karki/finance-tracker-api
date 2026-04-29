package com.upkdev.financialtracker.domain.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upkdev.financialtracker.domain.auth.api.AuthController;
import com.upkdev.financialtracker.domain.auth.dto.*;
import com.upkdev.financialtracker.domain.auth.service.AuthService;
import com.upkdev.financialtracker.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = AuthController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  AuthService authService;
    @MockBean  JwtUtil jwtUtil;

    private AuthResponse buildAuthResponse(boolean verified) {
        return AuthResponse.builder()
                .memberId(1L)
                .firstName("John")
                .email("john@test.com")
                .emailVerified(verified)
                .token(verified ? "jwt-token" : null)
                .authProvider("LOCAL")
                .build();
    }

    @Test
    void register_validRequest_returns200() throws Exception {
        when(authService.register(any())).thenReturn(buildAuthResponse(false));

        RegisterRequest req = RegisterRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@test.com").password("pass1234")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john@test.com"));
    }

    @Test
    void register_duplicateEmail_returns400() throws Exception {
        when(authService.register(any())).thenThrow(new RuntimeException("An account with this email already exists"));

        RegisterRequest req = RegisterRequest.builder()
                .firstName("John").lastName("Doe")
                .email("dup@test.com").password("pass1234")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_validCredentials_returnsToken() throws Exception {
        when(authService.login(any())).thenReturn(buildAuthResponse(true));

        LoginRequest req = LoginRequest.builder()
                .email("john@test.com").password("pass1234")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    void login_unverifiedEmail_returns403() throws Exception {
        when(authService.login(any())).thenThrow(new RuntimeException("EMAIL_NOT_VERIFIED"));

        LoginRequest req = LoginRequest.builder()
                .email("john@test.com").password("pass1234")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        when(authService.login(any())).thenThrow(new RuntimeException("Invalid email or password"));

        LoginRequest req = LoginRequest.builder()
                .email("john@test.com").password("wrong")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyOtp_validCode_returns200() throws Exception {
        when(authService.verifyOtp(any())).thenReturn(buildAuthResponse(true));

        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("john@test.com");
        req.setOtpCode("123456");

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.emailVerified").value(true));
    }

    @Test
    void verifyOtp_invalidCode_returns400() throws Exception {
        when(authService.verifyOtp(any())).thenThrow(new RuntimeException("Invalid OTP code"));

        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("john@test.com");
        req.setOtpCode("000000");

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resendOtp_success_returns200() throws Exception {
        doNothing().when(authService).resendOtp("john@test.com");

        mockMvc.perform(post("/api/auth/resend-otp")
                        .param("email", "john@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void resendOtp_alreadyVerified_returns400() throws Exception {
        doThrow(new RuntimeException("Email already verified")).when(authService).resendOtp(anyString());

        mockMvc.perform(post("/api/auth/resend-otp")
                        .param("email", "john@test.com"))
                .andExpect(status().isBadRequest());
    }
}
