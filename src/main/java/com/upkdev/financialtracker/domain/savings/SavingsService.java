package com.upkdev.financialtracker.domain.savings;

import com.upkdev.financialtracker.domain.expense.Expense;
import com.upkdev.financialtracker.domain.expense.ExpenseRepository;
import com.upkdev.financialtracker.domain.savings.dto.SavingsGoalRequest;
import com.upkdev.financialtracker.domain.savings.dto.SavingsRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingsService {

    private final SavingsRepository savingsRepository;
    private final ExpenseRepository expenseRepository;

    public SavingsGoal createGoal(SavingsGoalRequest request) {
        SavingsGoal goal = SavingsGoal.builder()
                .memberId(request.getMemberId())
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .income(request.getIncome())
                .monthlySavingsTarget(request.getMonthlySavingsTarget())
                .targetDate(request.getTargetDate())
                .build();
        return savingsRepository.save(goal);
    }

    public List<SavingsGoal> getGoalsByMember(Long memberId) {
        return savingsRepository.findByMemberId(memberId);
    }

    public SavingsRecommendationResponse getRecommendations(Long memberId) {
        List<Expense> expenses = expenseRepository.findByMemberId(memberId);

        // Category breakdown
        Map<String, BigDecimal> categoryBreakdown = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory() != null ? e.getCategory().name() : "OTHER",
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        BigDecimal totalExpenses = categoryBreakdown.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get income from latest active savings goal
        List<SavingsGoal> goals = savingsRepository.findByMemberId(memberId);
        BigDecimal income = goals.stream()
                .filter(g -> "ACTIVE".equals(g.getStatus()) && g.getIncome() != null)
                .map(SavingsGoal::getIncome)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        BigDecimal currentMonthlySavings = income.subtract(totalExpenses);

        // Top 3 spending categories
        List<String> topSpendingCategories = categoryBreakdown.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Target amount from first active goal
        BigDecimal targetAmount = goals.stream()
                .filter(g -> "ACTIVE".equals(g.getStatus()) && g.getTargetAmount() != null)
                .map(SavingsGoal::getTargetAmount)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        // Projected months
        Map<String, Integer> projectedMonthsToGoal = new LinkedHashMap<>();
        projectedMonthsToGoal.put("at_current_rate", projectMonths(targetAmount, currentMonthlySavings));

        if (!topSpendingCategories.isEmpty()) {
            String topCategory = topSpendingCategories.get(0);
            BigDecimal topCategorySpend = categoryBreakdown.getOrDefault(topCategory, BigDecimal.ZERO);

            BigDecimal reduction10 = topCategorySpend.multiply(BigDecimal.valueOf(0.10));
            BigDecimal reduction20 = topCategorySpend.multiply(BigDecimal.valueOf(0.20));

            projectedMonthsToGoal.put("with_10pct_reduction", projectMonths(targetAmount, currentMonthlySavings.add(reduction10)));
            projectedMonthsToGoal.put("with_20pct_reduction", projectMonths(targetAmount, currentMonthlySavings.add(reduction20)));
        }

        // Recommendations
        List<String> recommendations = new ArrayList<>();
        if (income.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal savingsRate = currentMonthlySavings.divide(income, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            recommendations.add(String.format("Your current savings rate is %.1f%% of income.", savingsRate.doubleValue()));
        }

        if (!topSpendingCategories.isEmpty()) {
            String topCategory = topSpendingCategories.get(0);
            BigDecimal topSpend = categoryBreakdown.getOrDefault(topCategory, BigDecimal.ZERO);
            BigDecimal save10 = topSpend.multiply(BigDecimal.valueOf(0.10)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal save20 = topSpend.multiply(BigDecimal.valueOf(0.20)).setScale(2, RoundingMode.HALF_UP);

            int current = projectedMonthsToGoal.getOrDefault("at_current_rate", -1);
            int with10 = projectedMonthsToGoal.getOrDefault("with_10pct_reduction", -1);
            int with20 = projectedMonthsToGoal.getOrDefault("with_20pct_reduction", -1);

            if (current > 0 && with10 > 0) {
                int diff = current - with10;
                recommendations.add(String.format(
                        "Reducing %s spending by 10%% would save $%.2f/month and get you to your goal %d month(s) faster.",
                        topCategory, save10.doubleValue(), diff));
            }
            if (current > 0 && with20 > 0) {
                int diff = current - with20;
                recommendations.add(String.format(
                        "Reducing %s spending by 20%% would save $%.2f/month and get you to your goal %d month(s) faster.",
                        topCategory, save20.doubleValue(), diff));
            }
        }

        if (currentMonthlySavings.compareTo(BigDecimal.ZERO) <= 0) {
            recommendations.add("Your expenses exceed your income. Review your spending to start saving.");
        }

        return SavingsRecommendationResponse.builder()
                .memberId(memberId)
                .totalIncome(income)
                .totalExpenses(totalExpenses)
                .currentMonthlySavings(currentMonthlySavings)
                .categoryBreakdown(categoryBreakdown)
                .topSpendingCategories(topSpendingCategories)
                .recommendations(recommendations)
                .projectedMonthsToGoal(projectedMonthsToGoal)
                .build();
    }

    private int projectMonths(BigDecimal target, BigDecimal monthlySavings) {
        if (target == null || target.compareTo(BigDecimal.ZERO) <= 0) return -1;
        if (monthlySavings == null || monthlySavings.compareTo(BigDecimal.ZERO) <= 0) return -1;
        return target.divide(monthlySavings, 0, RoundingMode.CEILING).intValue();
    }
}
