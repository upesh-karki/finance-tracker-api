package com.upkdev.financialtracker.domain.auth;

import com.upkdev.financialtracker.domain.auth.dto.*;
import com.upkdev.financialtracker.domain.auth.entity.EmailOtp;
import com.upkdev.financialtracker.domain.auth.repository.EmailOtpRepository;
import com.upkdev.financialtracker.domain.auth.service.impl.AuthServiceImpl;
import com.upkdev.financialtracker.domain.member.entity.Member;
import com.upkdev.financialtracker.domain.member.repository.MemberRepository;
import com.upkdev.financialtracker.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock EmailOtpRepository otpRepository;
    @Mock JwtUtil jwtUtil;
    @Mock RestTemplate restTemplate;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "otpExpiryMinutes", 10);
        ReflectionTestUtils.setField(authService, "googleClientId", "test-google-client-id");
        ReflectionTestUtils.setField(authService, "brevoApiKey", "test-key");
        ReflectionTestUtils.setField(authService, "brevoSenderEmail", "test@test.com");
        ReflectionTestUtils.setField(authService, "brevoSenderName", "Test");
    }

    private Member buildMember(Long id, boolean verified) {
        return Member.builder()
                .id(id)
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .password("hashed")
                .username("john@test.com")
                .emailVerified(verified)
                .authProvider("LOCAL")
                .profileStatus(verified ? "ACTIVE" : "PENDING_VERIFICATION")
                .build();
    }

    // ── register ────────────────────────────────────────────────

    @Test
    void register_newEmail_savesAndSendsOtp() {
        when(memberRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        Member saved = buildMember(1L, false);
        saved.setEmail("new@test.com");
        when(memberRepository.save(any(Member.class))).thenReturn(saved);
        doNothing().when(otpRepository).deleteByMemberId(any());
        when(otpRepository.save(any(EmailOtp.class))).thenAnswer(i -> i.getArgument(0));
        // Brevo call — let RestTemplate return null (fire-and-forget)
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(null);

        RegisterRequest req = RegisterRequest.builder()
                .firstName("John").lastName("Doe")
                .email("new@test.com").password("pass1234")
                .build();

        AuthResponse resp = authService.register(req);

        assertThat(resp.getEmail()).isEqualTo("new@test.com");
        assertThat(resp.isEmailVerified()).isFalse();
        verify(memberRepository).save(any(Member.class));
        verify(otpRepository).save(any(EmailOtp.class));
    }

    @Test
    void register_duplicateEmail_throwsRuntimeException() {
        when(memberRepository.existsByEmail("existing@test.com")).thenReturn(true);

        RegisterRequest req = RegisterRequest.builder()
                .email("existing@test.com").password("pass").build();

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    // ── login ────────────────────────────────────────────────────

    @Test
    void login_validVerifiedMember_returnsTokenResponse() {
        Member member = buildMember(1L, true);
        when(memberRepository.findByEmail("john@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("pass1234", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "john@test.com")).thenReturn("jwt-token");

        LoginRequest req = new LoginRequest();
        req.setEmail("john@test.com");
        req.setPassword("pass1234");

        AuthResponse resp = authService.login(req);

        assertThat(resp.getToken()).isEqualTo("jwt-token");
        assertThat(resp.isEmailVerified()).isTrue();
    }

    @Test
    void login_wrongPassword_throwsRuntimeException() {
        Member member = buildMember(1L, true);
        when(memberRepository.findByEmail("john@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        LoginRequest req = new LoginRequest();
        req.setEmail("john@test.com");
        req.setPassword("wrong");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void login_emailNotVerified_throwsEmailNotVerifiedAndSendsOtp() {
        Member member = buildMember(1L, false);
        when(memberRepository.findByEmail("john@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        doNothing().when(otpRepository).deleteByMemberId(any());
        when(otpRepository.save(any(EmailOtp.class))).thenAnswer(i -> i.getArgument(0));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(null);

        LoginRequest req = new LoginRequest();
        req.setEmail("john@test.com");
        req.setPassword("pass1234");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("EMAIL_NOT_VERIFIED");
    }

    @Test
    void login_memberNotFound_throwsRuntimeException() {
        when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());

        LoginRequest req = new LoginRequest();
        req.setEmail("ghost@test.com");
        req.setPassword("pass");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class);
    }

    // ── verifyOtp ────────────────────────────────────────────────

    @Test
    void verifyOtp_validCode_verifiesAndReturnsToken() {
        Member member = buildMember(1L, false);
        when(memberRepository.findByEmail("john@test.com")).thenReturn(Optional.of(member));

        EmailOtp otp = EmailOtp.builder()
                .memberId(1L)
                .otpCode("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
        when(otpRepository.findTopByMemberIdAndUsedFalseOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(otp));
        when(otpRepository.save(any(EmailOtp.class))).thenAnswer(i -> i.getArgument(0));
        when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtUtil.generateToken(1L, "john@test.com")).thenReturn("jwt-token");

        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("john@test.com");
        req.setOtpCode("123456");

        AuthResponse resp = authService.verifyOtp(req);

        assertThat(resp.getToken()).isEqualTo("jwt-token");
        assertThat(resp.isEmailVerified()).isTrue();
        assertThat(otp.getUsed()).isTrue();
    }

    @Test
    void verifyOtp_wrongCode_throwsRuntimeException() {
        Member member = buildMember(1L, false);
        when(memberRepository.findByEmail("john@test.com")).thenReturn(Optional.of(member));

        EmailOtp otp = EmailOtp.builder()
                .otpCode("999999")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
        when(otpRepository.findTopByMemberIdAndUsedFalseOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(otp));

        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("john@test.com");
        req.setOtpCode("000000");

        assertThatThrownBy(() -> authService.verifyOtp(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid OTP");
    }

    @Test
    void verifyOtp_expiredOtp_throwsRuntimeException() {
        Member member = buildMember(1L, false);
        when(memberRepository.findByEmail("john@test.com")).thenReturn(Optional.of(member));

        EmailOtp otp = EmailOtp.builder()
                .otpCode("123456")
                .expiresAt(LocalDateTime.now().minusMinutes(1))  // already expired
                .used(false)
                .build();
        when(otpRepository.findTopByMemberIdAndUsedFalseOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(otp));

        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("john@test.com");
        req.setOtpCode("123456");

        assertThatThrownBy(() -> authService.verifyOtp(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expired");
    }

    // ── resendOtp ────────────────────────────────────────────────

    @Test
    void resendOtp_unverifiedMember_sendsNewOtp() {
        Member member = buildMember(1L, false);
        when(memberRepository.findByEmail("john@test.com")).thenReturn(Optional.of(member));
        doNothing().when(otpRepository).deleteByMemberId(1L);
        when(otpRepository.save(any(EmailOtp.class))).thenAnswer(i -> i.getArgument(0));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(null);

        authService.resendOtp("john@test.com");

        verify(otpRepository).save(any(EmailOtp.class));
    }

    @Test
    void resendOtp_alreadyVerified_throwsRuntimeException() {
        Member member = buildMember(1L, true);
        when(memberRepository.findByEmail("john@test.com")).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> authService.resendOtp("john@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already verified");
    }
}
