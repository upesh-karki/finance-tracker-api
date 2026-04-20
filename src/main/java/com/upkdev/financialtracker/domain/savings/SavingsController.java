package com.upkdev.financialtracker.domain.savings;

import com.upkdev.financialtracker.domain.savings.dto.SavingsGoalRequest;
import com.upkdev.financialtracker.domain.savings.dto.SavingsRecommendationResponse;
import com.upkdev.financialtracker.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/savings")
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsService savingsService;

    @PostMapping("/goals")
    public ResponseEntity<ApiResponse<SavingsGoal>> createGoal(@Valid @RequestBody SavingsGoalRequest request) {
        SavingsGoal goal = savingsService.createGoal(request);
        return ResponseEntity.ok(ApiResponse.ok("Savings goal created", goal));
    }

    @GetMapping("/goals/member/{memberId}")
    public ResponseEntity<ApiResponse<List<SavingsGoal>>> getGoalsByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.ok(savingsService.getGoalsByMember(memberId)));
    }

    @GetMapping("/recommendations/{memberId}")
    public ResponseEntity<ApiResponse<SavingsRecommendationResponse>> getRecommendations(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.ok(savingsService.getRecommendations(memberId)));
    }
}
