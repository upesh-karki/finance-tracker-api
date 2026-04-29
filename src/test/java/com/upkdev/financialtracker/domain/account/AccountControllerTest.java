package com.upkdev.financialtracker.domain.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upkdev.financialtracker.domain.account.api.AccountController;
import com.upkdev.financialtracker.domain.account.dto.FinancialAccountRequest;
import com.upkdev.financialtracker.domain.account.dto.FinancialAccountResponse;
import com.upkdev.financialtracker.domain.account.entity.FinancialInstitution;
import com.upkdev.financialtracker.domain.account.repository.FinancialInstitutionRepository;
import com.upkdev.financialtracker.domain.account.service.AccountService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = AccountController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  AccountService accountService;
    @MockBean  FinancialInstitutionRepository institutionRepository;
    @MockBean  JwtUtil jwtUtil;

    private FinancialAccountResponse buildResponse(Long id) {
        return FinancialAccountResponse.builder()
                .id(id).memberId(1L)
                .nickname("Chequing").institutionName("TD Bank")
                .accountTypeCode("CHEQUING").isActive(true)
                .missingMonths(List.of())
                .build();
    }

    private FinancialAccountRequest buildRequest() {
        return FinancialAccountRequest.builder()
                .memberId(1L).nickname("Chequing")
                .institutionName("TD Bank").accountTypeCode("CHEQUING")
                .openedDate(LocalDate.now().minusYears(1))
                .build();
    }

    @Test
    void createAccount_returns200() throws Exception {
        when(accountService.createAccount(any())).thenReturn(buildResponse(1L));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nickname").value("Chequing"));
    }

    @Test
    void createAccount_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAccounts_returns200WithList() throws Exception {
        when(accountService.getAccountsForMember(1L)).thenReturn(List.of(buildResponse(1L)));

        mockMvc.perform(get("/api/v1/accounts/member/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getAccount_returns200() throws Exception {
        when(accountService.getAccount(1L)).thenReturn(buildResponse(1L));

        mockMvc.perform(get("/api/v1/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberId").value(1));
    }

    @Test
    void deactivateAccount_returns200() throws Exception {
        doNothing().when(accountService).deactivateAccount(1L);

        mockMvc.perform(delete("/api/v1/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getMissingMonths_returns200() throws Exception {
        FinancialAccountResponse.MissingStatementMonth missing =
                FinancialAccountResponse.MissingStatementMonth.builder()
                        .year(2024).month(1).monthLabel("January 2024").build();
        when(accountService.getMissingMonths(1L)).thenReturn(List.of(missing));

        mockMvc.perform(get("/api/v1/accounts/1/missing-months"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getInstitutions_returns200() throws Exception {
        FinancialInstitution inst = FinancialInstitution.builder()
                .code("TD").name("TD Bank").country("CA").isActive(true).build();
        when(institutionRepository.findByIsActiveTrueOrderByName()).thenReturn(List.of(inst));

        mockMvc.perform(get("/api/v1/accounts/institutions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("TD"));
    }
}
