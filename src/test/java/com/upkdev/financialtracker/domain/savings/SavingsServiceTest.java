package com.upkdev.financialtracker.domain.savings;

import com.upkdev.financialtracker.domain.expense.Expense;
import com.upkdev.financialtracker.domain.expense.ExpenseCategory;
import com.upkdev.financialtracker.domain.expense.ExpenseRepository;
import com.upkdev.financialtracker.domain.savings.dto.SavingsGoalRequest;
import com.upkdev.financialtracker.domain.savings.dto.SavingsRecommendationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsServiceTest {

    @Mock
    private SavingsRepository savingsRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private SavingsService savingsService;

    private SavingsGoalRequest buildGoalRequest() {
        SavingsGoalRequest req = new SavingsGoalRequest();
        req.setMemberId(1L);
        req.setGoalName("Emergency Fund");
        req.setTargetAmount(new BigDecimal("5000.00"));
        req.setIncome(new BigDecimal("3000.00"));
        req.setMonthlySavingsTarget(new BigDecimal("500.00"));
        req.setTargetDate(LocalDate.now().plusMonths(10));
        return req;
    }

    private SavingsGoal buildGoal(Long id) {
        return SavingsGoal.builder()
                .id(id)
                .memberId(1L)
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("5000.00"))
                .income(new BigDecimal("3000.00"))
                .monthlySavingsTarget(new BigDecimal("500.00"))
                .status("ACTIVE")
                .build();
    }

    @Test
    void createGoal_savesAndReturns() {
        SavingsGoal saved = buildGoal(1L);
        when(savingsRepository.save(any(SavingsGoal.class))).thenReturn(saved);

        SavingsGoal result = savingsService.createGoal(buildGoalRequest());

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getGoalName()).isEqualTo("Emergency Fund");
        verify(savingsRepository).save(any(SavingsGoal.class));
    }

    @Test
    void getGoalsByMember_returnsList() {
        when(savingsRepository.findByMemberId(1L)).thenReturn(List.of(buildGoal(1L), buildGoal(2L)));

        List<SavingsGoal> goals = savingsService.getGoalsByMember(1L);

        assertThat(goals).hasSize(2);
    }

    @Test
    void getRecommendations_returnsCorrectData() {
        List<Expense> expenses = List.of(
                Expense.builder().memberId(1L).expenseName("Food").amount(new BigDecimal("400.00"))
                        .category(ExpenseCategory.FOOD).expenseDate(LocalDate.now()).build(),
                Expense.builder().memberId(1L).expenseName("Transport").amount(new BigDecimal("200.00"))
                        .category(ExpenseCategory.TRANSPORT).expenseDate(LocalDate.now()).build()
        );
        when(expenseRepository.findByMemberId(1L)).thenReturn(expenses);
        when(savingsRepository.findByMemberId(1L)).thenReturn(List.of(buildGoal(1L)));

        SavingsRecommendationResponse response = savingsService.getRecommendations(1L);

        // totalExpenses = 400 + 200 = 600
        assertThat(response.getTotalExpenses()).isEqualByComparingTo("600.00");

        // currentMonthlySavings = income(3000) - expenses(600) = 2400
        assertThat(response.getCurrentMonthlySavings()).isEqualByComparingTo("2400.00");

        // recommendations should be non-empty
        assertThat(response.getRecommendations()).isNotEmpty();

        // projectedMonthsToGoal should be non-null
        assertThat(response.getProjectedMonthsToGoal()).isNotNull();
    }
}
