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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.otp.expiry-minutes}")
    private int otpExpiryMinutes;

    @Value("${app.google.client-id}")
    private String googleClientId;

    @Value("${app.smtp.from}")
    private String smtpFrom;

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

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(smtpFrom);
        message.setTo(member.getEmail());
        message.setSubject("Finance Tracker \u2014 Verify your email");
        message.setText(
            "Hi " + member.getFirstName() + ",\n\n" +
            "Your verification code is:\n\n" +
            "  " + code + "\n\n" +
            "This code expires in " + otpExpiryMinutes + " minutes.\n\n" +
            "If you didn't request this, please ignore this email.\n\n" +
            "\u2014 Finance Tracker"
        );
        mailSender.send(message);
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
