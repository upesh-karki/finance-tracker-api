package com.upkdev.financialtracker.domain.savings.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsRecommendationResponse {
    private Long memberId;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal currentMonthlySavings;
    private Map<String, BigDecimal> categoryBreakdown;
    private List<String> topSpendingCategories;
    private List<String> recommendations;
    private Map<String, Integer> projectedMonthsToGoal;
}
