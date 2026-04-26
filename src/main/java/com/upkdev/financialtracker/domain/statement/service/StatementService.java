package com.upkdev.financialtracker.domain.statement.service;

import com.upkdev.financialtracker.domain.statement.dto.StatementUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface StatementService {
    StatementUploadResponse processStatement(MultipartFile file);
    StatementUploadResponse processStatement(MultipartFile file, String accountTypeCode);
}
