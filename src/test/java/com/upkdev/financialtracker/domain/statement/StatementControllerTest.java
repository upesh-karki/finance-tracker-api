package com.upkdev.financialtracker.domain.statement;

import com.upkdev.financialtracker.domain.account.dto.AccountStatementResponse;
import com.upkdev.financialtracker.domain.statement.api.StatementController;
import com.upkdev.financialtracker.domain.statement.dto.StatementUploadResponse;
import com.upkdev.financialtracker.domain.statement.service.StatementService;
import com.upkdev.financialtracker.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = StatementController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@ActiveProfiles("test")
class StatementControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  StatementService statementService;
    @MockBean  JwtUtil jwtUtil;

    private StatementUploadResponse buildUploadResponse() {
        return StatementUploadResponse.builder()
                .transactionCount(10)
                .expenseCount(7)
                .incomeCount(2)
                .transferCount(1)
                .investmentCount(0)
                .modelUsed("gemma4:e2b")
                .build();
    }

    @Test
    void uploadStatement_validPdf_returns200() throws Exception {
        when(statementService.processStatement(any(), any())).thenReturn(buildUploadResponse());

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "statement.pdf", "application/pdf", "fake-pdf-content".getBytes());

        mockMvc.perform(multipart("/api/statements/upload").file(pdf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionCount").value(10));
    }

    @Test
    void uploadStatement_emptyFile_returns400() throws Exception {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/api/statements/upload").file(empty))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void uploadStatement_notPdf_returns400() throws Exception {
        MockMultipartFile txt = new MockMultipartFile(
                "file", "data.txt", "text/plain", "some text".getBytes());

        mockMvc.perform(multipart("/api/statements/upload").file(txt))
                .andExpect(status().isBadRequest());
    }

    @Test
    void markUploaded_returns200() throws Exception {
        doNothing().when(statementService).markMonthUploaded(1L, 2024, 1, 10);

        mockMvc.perform(post("/api/statements/mark-uploaded")
                        .param("accountId", "1")
                        .param("year", "2024")
                        .param("month", "1")
                        .param("transactionCount", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void markUploaded_serviceThrows_returns400() throws Exception {
        doThrow(new RuntimeException("Account not found"))
                .when(statementService).markMonthUploaded(anyLong(), anyInt(), anyInt(), anyInt());

        mockMvc.perform(post("/api/statements/mark-uploaded")
                        .param("accountId", "99")
                        .param("year", "2024")
                        .param("month", "1")
                        .param("transactionCount", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStatementStatus_returns200() throws Exception {
        AccountStatementResponse resp = AccountStatementResponse.builder()
                .accountId(1L).statementYear(2024).statementMonth(1)
                .status("UPLOADED").transactionCount(10)
                .build();
        when(statementService.getStatementStatus(1L, 2024, 1)).thenReturn(resp);

        mockMvc.perform(get("/api/statements/status")
                        .param("accountId", "1")
                        .param("year", "2024")
                        .param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UPLOADED"));
    }

    @Test
    void getStatementStatus_notFound_returns400() throws Exception {
        when(statementService.getStatementStatus(anyLong(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/statements/status")
                        .param("accountId", "99")
                        .param("year", "2024")
                        .param("month", "1"))
                .andExpect(status().isBadRequest());
    }
}
