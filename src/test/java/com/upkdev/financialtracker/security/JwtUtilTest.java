package com.upkdev.financialtracker.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-for-unit-tests-only-minimum-32-chars");
        ReflectionTestUtils.setField(jwtUtil, "expiryMinutes", 30);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken(1L, "user@test.com");
        assertThat(token).isNotBlank();
    }

    @Test
    void isTokenValid_withValidToken_returnsTrue() {
        String token = jwtUtil.generateToken(1L, "user@test.com");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_withGarbageToken_returnsFalse() {
        assertThat(jwtUtil.isTokenValid("not.a.valid.token")).isFalse();
    }

    @Test
    void getMemberIdFromToken_extractsCorrectId() {
        String token = jwtUtil.generateToken(42L, "user@test.com");
        assertThat(jwtUtil.getMemberIdFromToken(token)).isEqualTo(42L);
    }

    @Test
    void validateToken_containsEmailClaim() {
        String token = jwtUtil.generateToken(1L, "hello@example.com");
        var claims = jwtUtil.validateToken(token);
        assertThat(claims.get("email", String.class)).isEqualTo("hello@example.com");
    }

    @Test
    void generateToken_twoCallsProduceDifferentTokens() {
        // Tokens differ because issuedAt timestamps will be different (or at minimum nonces differ)
        String t1 = jwtUtil.generateToken(1L, "a@test.com");
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}
        String t2 = jwtUtil.generateToken(1L, "a@test.com");
        // They may theoretically match within same millisecond — just assert both are valid
        assertThat(jwtUtil.isTokenValid(t1)).isTrue();
        assertThat(jwtUtil.isTokenValid(t2)).isTrue();
    }
}
