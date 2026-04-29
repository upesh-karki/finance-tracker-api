package com.upkdev.financialtracker.domain.expense;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upkdev.financialtracker.domain.expense.api.ExpenseController;
import com.upkdev.financialtracker.domain.expense.dto.ExpenseRequest;
import com.upkdev.financialtracker.domain.expense.dto.ExpenseResponse;
import com.upkdev.financialtracker.domain.expense.service.ExpenseService;
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
    value = ExpenseController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@ActiveProfiles("test")
class ExpenseControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  ExpenseService expenseService;
    @MockBean  JwtUtil jwtUtil;

    private ExpenseResponse buildResponse(Long id) {
        return ExpenseResponse.builder()
                .id(id).memberId(1L)
                .expenseName("Groceries")
                .amount(new BigDecimal("50.00"))
                .category(ExpenseCategory.FOOD)
                .expenseDate(LocalDate.now())
                .build();
    }

    private ExpenseRequest buildRequest() {
        ExpenseRequest req = new ExpenseRequest();
        req.setMemberId(1L);
        req.setExpenseName("Groceries");
        req.setAmount(new BigDecimal("50.00"));
        req.setExpenseDate(LocalDate.now());
        return req;
    }

    @Test
    void create_validRequest_returns200() throws Exception {
        when(expenseService.create(any())).thenReturn(buildResponse(1L));

        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void create_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findByMember_returns200WithList() throws Exception {
        when(expenseService.findByMember(1L)).thenReturn(List.of(buildResponse(1L), buildResponse(2L)));

        mockMvc.perform(get("/api/v1/expenses/member/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void findByCategory_returns200() throws Exception {
        when(expenseService.findByCategory(1L, ExpenseCategory.FOOD)).thenReturn(List.of(buildResponse(1L)));

        mockMvc.perform(get("/api/v1/expenses/member/1/category/FOOD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getTotal_returns200WithAmount() throws Exception {
        when(expenseService.getTotalByMember(1L)).thenReturn(new BigDecimal("150.00"));

        mockMvc.perform(get("/api/v1/expenses/member/1/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(150.00));
    }

    @Test
    void delete_returns200() throws Exception {
        doNothing().when(expenseService).deleteById(1L);

        mockMvc.perform(delete("/api/v1/expenses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
