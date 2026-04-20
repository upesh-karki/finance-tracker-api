package com.upkdev.financialtracker.domain.expense.api;

import com.upkdev.financialtracker.domain.expense.ExpenseCategory;
import com.upkdev.financialtracker.domain.expense.dto.ExpenseRequest;
import com.upkdev.financialtracker.domain.expense.dto.ExpenseResponse;
import com.upkdev.financialtracker.domain.expense.service.ExpenseService;
import com.upkdev.financialtracker.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> create(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.create(request);
        return ResponseEntity.ok(ApiResponse.ok("Expense created", response));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> findByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.ok(expenseService.findByMember(memberId)));
    }

    @GetMapping("/member/{memberId}/category/{category}")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> findByCategory(
            @PathVariable Long memberId,
            @PathVariable ExpenseCategory category) {
        return ResponseEntity.ok(ApiResponse.ok(expenseService.findByCategory(memberId, category)));
    }

    @GetMapping("/member/{memberId}/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotal(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.ok(expenseService.getTotalByMember(memberId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        expenseService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Expense deleted", null));
    }
}
