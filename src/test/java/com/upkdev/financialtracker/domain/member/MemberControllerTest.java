package com.upkdev.financialtracker.domain.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upkdev.financialtracker.domain.member.api.MemberController;
import com.upkdev.financialtracker.domain.member.dto.LoginRequest;
import com.upkdev.financialtracker.domain.member.dto.MemberRequest;
import com.upkdev.financialtracker.domain.member.dto.MemberResponse;
import com.upkdev.financialtracker.domain.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@ActiveProfiles("test")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    private MemberResponse buildResponse(Long id) {
        return MemberResponse.builder()
                .id(id)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .username("johndoe")
                .occupation("Engineer")
                .profileStatus("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void register_returns200WithSuccess() throws Exception {
        MemberRequest req = new MemberRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@example.com");
        req.setPassword("pass");
        req.setUsername("johndoe");

        when(memberService.register(any(MemberRequest.class))).thenReturn(buildResponse(1L));

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void findById_returns200WithMemberData() throws Exception {
        when(memberService.findById(1L)).thenReturn(buildResponse(1L));

        mockMvc.perform(get("/api/v1/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("johndoe"));
    }

    @Test
    void findAll_returns200WithList() throws Exception {
        when(memberService.findAll()).thenReturn(List.of(buildResponse(1L), buildResponse(2L)));

        mockMvc.perform(get("/api/v1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void login_returns200WithMemberData() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("johndoe");
        req.setPassword("pass");

        when(memberService.login(any(LoginRequest.class))).thenReturn(buildResponse(1L));

        mockMvc.perform(post("/api/v1/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("johndoe"));
    }

    @Test
    void register_invalidBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
