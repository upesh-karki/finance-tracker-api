package com.upkdev.financialtracker.domain.savings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upkdev.financialtracker.domain.savings.api.SavingsController;
import com.upkdev.financialtracker.domain.savings.dto.SavingsGoalRequest;
import com.upkdev.financialtracker.domain.savings.dto.SavingsGoalResponse;
import com.upkdev.financialtracker.domain.savings.dto.SavingsRecommendationResponse;
import com.upkdev.financialtracker.domain.savings.service.SavingsService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = SavingsController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@ActiveProfiles("test")
class SavingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  SavingsService savingsService;
    @MockBean  JwtUtil jwtUtil;

    private SavingsGoalResponse buildGoalResponse(Long id) {
        return SavingsGoalResponse.builder()
                .id(id).memberId(1L)
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("10000.00"))
                .currentAmount(BigDecimal.ZERO)
                .status("IN_PROGRESS")
                .build();
    }

    private SavingsGoalRequest buildGoalRequest() {
        SavingsGoalRequest req = new SavingsGoalRequest();
        req.setMemberId(1L);
        req.setGoalName("Emergency Fund");
        req.setTargetAmount(new BigDecimal("10000.00"));
        return req;
    }

    @Test
    void createGoal_validRequest_returns200() throws Exception {
        when(savingsService.createGoal(any())).thenReturn(buildGoalResponse(1L));

        mockMvc.perform(post("/api/v1/savings/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildGoalRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.goalName").value("Emergency Fund"));
    }

    @Test
    void createGoal_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/savings/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGoalsByMember_returns200WithList() throws Exception {
        when(savingsService.getGoalsByMember(1L)).thenReturn(List.of(buildGoalResponse(1L), buildGoalResponse(2L)));

        mockMvc.perform(get("/api/v1/savings/goals/member/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getRecommendations_returns200() throws Exception {
        SavingsRecommendationResponse rec = SavingsRecommendationResponse.builder()
                .memberId(1L)
                .totalIncome(new BigDecimal("5000.00"))
                .totalExpenses(new BigDecimal("3000.00"))
                .currentMonthlySavings(new BigDecimal("2000.00"))
                .build();
        when(savingsService.getRecommendations(1L)).thenReturn(rec);

        mockMvc.perform(get("/api/v1/savings/recommendations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberId").value(1));
    }
}
