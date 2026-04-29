package com.upkdev.financialtracker.domain.income;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upkdev.financialtracker.domain.income.api.IncomeController;
import com.upkdev.financialtracker.domain.income.dto.IncomeRequest;
import com.upkdev.financialtracker.domain.income.dto.IncomeResponse;
import com.upkdev.financialtracker.domain.income.service.IncomeService;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = IncomeController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@ActiveProfiles("test")
class IncomeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  IncomeService incomeService;
    @MockBean  JwtUtil jwtUtil;

    private IncomeResponse buildResponse(Long id) {
        return IncomeResponse.builder()
                .id(id).memberId(1L)
                .sourceName("Employer")
                .amount(new BigDecimal("3000.00"))
                .incomeCategoryCode("SALARY")
                .incomeDate(LocalDate.now())
                .build();
    }

    private IncomeRequest buildRequest() {
        return IncomeRequest.builder()
                .memberId(1L)
                .sourceName("Employer")
                .amount(new BigDecimal("3000.00"))
                .incomeCategoryCode("SALARY")
                .incomeDate(LocalDate.now())
                .build();
    }

    @Test
    void addIncome_validRequest_returns200() throws Exception {
        when(incomeService.addIncome(any())).thenReturn(buildResponse(1L));

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc.perform(post("/api/v1/income")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void addIncome_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/income")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getIncome_returns200WithList() throws Exception {
        when(incomeService.getIncomeForMember(1L)).thenReturn(List.of(buildResponse(1L)));

        mockMvc.perform(get("/api/v1/income/member/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getIncomeByCategory_returns200() throws Exception {
        when(incomeService.getIncomeByCategory(1L, "SALARY")).thenReturn(List.of(buildResponse(1L)));

        mockMvc.perform(get("/api/v1/income/member/1/category/SALARY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void deleteIncome_returns200() throws Exception {
        doNothing().when(incomeService).deleteIncome(1L);

        mockMvc.perform(delete("/api/v1/income/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
