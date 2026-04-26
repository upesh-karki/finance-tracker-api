package com.upkdev.financialtracker.domain.account.api;

import com.upkdev.financialtracker.domain.account.dto.*;
import com.upkdev.financialtracker.domain.account.repository.FinancialInstitutionRepository;
import com.upkdev.financialtracker.domain.account.service.AccountService;
import com.upkdev.financialtracker.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final FinancialInstitutionRepository institutionRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<FinancialAccountResponse>> createAccount(
            @Valid @RequestBody FinancialAccountRequest request) {
        return ResponseEntity.ok(ApiResponse.success(accountService.createAccount(request)));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<List<FinancialAccountResponse>>> getAccounts(
            @PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAccountsForMember(memberId)));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<FinancialAccountResponse>> getAccount(
            @PathVariable Long accountId) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAccount(accountId)));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @PathVariable Long accountId) {
        accountService.deactivateAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{accountId}/missing-months")
    public ResponseEntity<ApiResponse<List<FinancialAccountResponse.MissingStatementMonth>>> getMissingMonths(
            @PathVariable Long accountId) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getMissingMonths(accountId)));
    }

    @GetMapping("/institutions")
    public ResponseEntity<ApiResponse<List<FinancialInstitutionResponse>>> getInstitutions() {
        List<FinancialInstitutionResponse> list = institutionRepository.findByIsActiveTrueOrderByName()
                .stream()
                .map(i -> FinancialInstitutionResponse.builder()
                        .code(i.getCode()).name(i.getName()).country(i.getCountry()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
