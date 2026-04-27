package com.upkdev.financialtracker.integration;

import com.upkdev.financialtracker.domain.member.dto.LoginRequest;
import com.upkdev.financialtracker.domain.member.dto.MemberRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests — boots a real server on a random port against the test DB (H2).
 *
 * Run from IDE: right-click → Run, or via Maven:
 *   mvn test -Dtest=ApiIntegrationTest
 *
 * These tests cover the full HTTP request/response cycle including
 * Spring Security, JSON serialisation, and validation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("API Integration Tests — live server")
class ApiIntegrationTest {

    @LocalServerPort
    private int port;

    /** JWT token obtained during login — shared across ordered tests */
    private static String jwtToken;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port    = port;
    }

    // ─────────────────────────────────────────────────────────────
    // AUTH
    // ─────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/members — register a new member")
    void register_returnsOk() {
        MemberRequest req = new MemberRequest();
        req.setFirstName("Integration");
        req.setLastName("Test");
        req.setEmail("integration@test.com");
        req.setPassword("test1234");
        req.setUsername("integrationtest");
        req.setOccupation("QA");
        req.setPhoneNumber("555-0000");

        given()
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/v1/members")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.email", equalTo("integration@test.com"))
            .body("data.username", equalTo("integrationtest"));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/members/login — bad credentials returns 404")
    void login_wrongCredentials_returns404() {
        LoginRequest req = new LoginRequest();
        req.setUsername("integrationtest");
        req.setPassword("wrongpassword");

        given()
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/v1/members/login")
        .then()
            .statusCode(anyOf(is(401), is(404)));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/accounts/institutions — public, returns institution list")
    void getInstitutions_returnsOk() {
        given()
        .when()
            .get("/api/v1/accounts/institutions")
        .then()
            .statusCode(200)
            .body("success", equalTo(true));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/members — requires JWT, returns 401 without token")
    void getMembers_withoutToken_returns401() {
        given()
        .when()
            .get("/api/v1/members")
        .then()
            .statusCode(401);
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/v1/members — invalid body returns 400")
    void register_invalidBody_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/members")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(6)
    @DisplayName("GET /api-docs — Swagger JSON endpoint is accessible")
    void swaggerDocs_accessible() {
        given()
        .when()
            .get("/api-docs")
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"));
    }
}
