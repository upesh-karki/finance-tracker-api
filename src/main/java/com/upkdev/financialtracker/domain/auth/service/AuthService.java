package com.upkdev.financialtracker.domain.auth.service;

import com.upkdev.financialtracker.domain.auth.dto.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse verifyOtp(VerifyOtpRequest request);
    void resendOtp(String email);
    AuthResponse googleAuth(GoogleAuthRequest request);
}
