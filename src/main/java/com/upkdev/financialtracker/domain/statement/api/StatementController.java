package com.upkdev.financialtracker.domain.statement.api;

import com.upkdev.financialtracker.domain.statement.dto.StatementUploadResponse;
import com.upkdev.financialtracker.domain.statement.service.StatementService;
import com.upkdev.financialtracker.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/statements")
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StatementUploadResponse>> uploadStatement(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "accountTypeCode", required = false) String accountTypeCode) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No file provided"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Only PDF files are accepted"));
        }
        StatementUploadResponse response = statementService.processStatement(file, accountTypeCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
