package com.upkdev.financialtracker.domain.income.api;

import com.upkdev.financialtracker.domain.income.dto.*;
import com.upkdev.financialtracker.domain.income.service.IncomeService;
import com.upkdev.financialtracker.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/income")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<ApiResponse<IncomeResponse>> addIncome(
            @Valid @RequestBody IncomeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(incomeService.addIncome(request)));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<List<IncomeResponse>>> getIncome(
            @PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(incomeService.getIncomeForMember(memberId)));
    }

    @GetMapping("/member/{memberId}/category/{categoryCode}")
    public ResponseEntity<ApiResponse<List<IncomeResponse>>> getIncomeByCategory(
            @PathVariable Long memberId,
            @PathVariable String categoryCode) {
        return ResponseEntity.ok(ApiResponse.success(incomeService.getIncomeByCategory(memberId, categoryCode)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
