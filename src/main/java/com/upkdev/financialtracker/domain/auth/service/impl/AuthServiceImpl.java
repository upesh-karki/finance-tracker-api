package com.upkdev.financialtracker.domain.auth.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.upkdev.financialtracker.domain.auth.dto.*;
import com.upkdev.financialtracker.domain.auth.entity.EmailOtp;
import com.upkdev.financialtracker.domain.auth.repository.EmailOtpRepository;
import com.upkdev.financialtracker.domain.auth.service.AuthService;
import com.upkdev.financialtracker.domain.member.entity.Member;
import com.upkdev.financialtracker.domain.member.repository.MemberRepository;
import com.upkdev.financialtracker.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final EmailOtpRepository otpRepository;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.otp.expiry-minutes}")
    private int otpExpiryMinutes;

    @Value("${app.google.client-id}")
    private String googleClientId;

    @Value("${app.brevo.api-key}")
    private String brevoApiKey;

    @Value("${app.brevo.sender-email}")
    private String brevoSenderEmail;

    @Value("${app.brevo.sender-name:Finance Tracker}")
    private String brevoSenderName;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists");
        }

        Member member = Member.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getEmail())
                .emailVerified(false)
                .authProvider("LOCAL")
                .profileStatus("PENDING_VERIFICATION")
                .build();

        member = memberRepository.save(member);
        sendOtp(member);

        return AuthResponse.builder()
                .memberId(member.getId())
                .firstName(member.getFirstName())
                .email(member.getEmail())
                .emailVerified(false)
                .authProvider("LOCAL")
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!Boolean.TRUE.equals(member.getEmailVerified())) {
            sendOtp(member);
            throw new RuntimeException("EMAIL_NOT_VERIFIED");
        }

        String token = jwtUtil.generateToken(member.getId(), member.getEmail());
        return buildAuthResponse(member, token);
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        EmailOtp otp = otpRepository
                .findTopByMemberIdAndUsedFalseOrderByCreatedAtDesc(member.getId())
                .orElseThrow(() -> new RuntimeException("No active OTP found. Please request a new one."));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        if (!otp.getOtpCode().equals(request.getOtpCode())) {
            throw new RuntimeException("Invalid OTP code");
        }

        otp.setUsed(true);
        otpRepository.save(otp);

        member.setEmailVerified(true);
        member.setProfileStatus("ACTIVE");
        memberRepository.save(member);

        String token = jwtUtil.generateToken(member.getId(), member.getEmail());
        return buildAuthResponse(member, token);
    }

    @Override
    public void resendOtp(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        if (Boolean.TRUE.equals(member.getEmailVerified())) {
            throw new RuntimeException("Email already verified");
        }
        sendOtp(member);
    }

    @Override
    @Transactional
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken == null) {
                throw new RuntimeException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String googleId = payload.getSubject();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");

            Member member = memberRepository.findByEmail(email).orElseGet(() -> {
                Member newMember = Member.builder()
                        .firstName(firstName != null ? firstName : "")
                        .lastName(lastName != null ? lastName : "")
                        .email(email)
                        .username(email)
                        .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                        .emailVerified(true)
                        .authProvider("GOOGLE")
                        .googleId(googleId)
                        .profileStatus("ACTIVE")
                        .build();
                return memberRepository.save(newMember);
            });

            if (member.getGoogleId() == null) {
                member.setGoogleId(googleId);
                member.setEmailVerified(true);
                member.setProfileStatus("ACTIVE");
                memberRepository.save(member);
            }

            String token = jwtUtil.generateToken(member.getId(), member.getEmail());
            return buildAuthResponse(member, token);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage(), e);
        }
    }

    private void sendOtp(Member member) {
        otpRepository.deleteByMemberId(member.getId());

        String code = String.format("%06d", new Random().nextInt(999999));
        EmailOtp otp = EmailOtp.builder()
                .memberId(member.getId())
                .otpCode(code)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        otpRepository.save(otp);

        sendBrevoEmail(
                member.getEmail(),
                member.getFirstName(),
                "Finance Tracker \u2014 Verify your email",
                "<html><body style='font-family:sans-serif;background:#11111b;color:#cdd6f4;padding:40px'>" +
                "<div style='max-width:480px;margin:0 auto;background:#1e1e2e;border-radius:12px;padding:36px'>" +
                "<h2 style='color:#89b4fa;margin-top:0'>Verify your email</h2>" +
                "<p>Hi " + member.getFirstName() + ",</p>" +
                "<p>Your Finance Tracker verification code is:</p>" +
                "<div style='background:#313244;border-radius:8px;padding:20px;text-align:center;margin:24px 0'>" +
                "<span style='font-size:2rem;font-weight:700;letter-spacing:0.3em;color:#cdd6f4'>" + code + "</span>" +
                "</div>" +
                "<p style='color:#a6adc8;font-size:0.9rem'>This code expires in <strong>" + otpExpiryMinutes + " minutes</strong>.</p>" +
                "<p style='color:#6c7086;font-size:0.8rem'>If you didn't create a Finance Tracker account, you can safely ignore this email.</p>" +
                "</div></body></html>"
        );
    }

    private void sendBrevoEmail(String toEmail, String toName, String subject, String htmlContent) {
        String url = "https://api.brevo.com/v3/smtp/email";

        String body = String.format(
            "{\"sender\":{\"name\":\"%s\",\"email\":\"%s\"}," +
            "\"to\":[{\"email\":\"%s\",\"name\":\"%s\"}]," +
            "\"subject\":\"%s\"," +
            "\"htmlContent\":\"%s\"}",
            brevoSenderName, brevoSenderEmail,
            toEmail, toName,
            subject,
            htmlContent.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
        );

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("api-key", brevoApiKey);
        headers.set("accept", "application/json");
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(body, headers);
        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        }
    }

    private AuthResponse buildAuthResponse(Member member, String token) {
        return AuthResponse.builder()
                .token(token)
                .memberId(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .email(member.getEmail())
                .emailVerified(Boolean.TRUE.equals(member.getEmailVerified()))
                .authProvider(member.getAuthProvider())
                .build();
    }
}
