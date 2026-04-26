package com.upkdev.financialtracker.domain.statement.service;

import com.upkdev.financialtracker.domain.statement.dto.StatementUploadResponse;
import com.upkdev.financialtracker.domain.account.dto.AccountStatementResponse;
import org.springframework.web.multipart.MultipartFile;

public interface StatementService {
    StatementUploadResponse processStatement(MultipartFile file);
    StatementUploadResponse processStatement(MultipartFile file, String accountTypeCode);
    void markMonthUploaded(Long accountId, int year, int month, int transactionCount);
    AccountStatementResponse getStatementStatus(Long accountId, int year, int month);
}
