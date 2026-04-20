package com.upkdev.financialtracker.domain.savings;

import com.upkdev.financialtracker.domain.expense.ExpenseCategory;
import com.upkdev.financialtracker.domain.expense.dao.ExpenseDao;
import com.upkdev.financialtracker.domain.expense.entity.Expense;
import com.upkdev.financialtracker.domain.savings.dao.SavingsDao;
import com.upkdev.financialtracker.domain.savings.dto.SavingsGoalRequest;
import com.upkdev.financialtracker.domain.savings.dto.SavingsGoalResponse;
import com.upkdev.financialtracker.domain.savings.dto.SavingsRecommendationResponse;
import com.upkdev.financialtracker.domain.savings.entity.SavingsGoal;
import com.upkdev.financialtracker.domain.savings.service.impl.SavingsServiceImpl;
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
    private SavingsDao savingsDao;

    @Mock
    private ExpenseDao expenseDao;

    @InjectMocks
    private SavingsServiceImpl savingsService;

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
        when(savingsDao.save(any(SavingsGoal.class))).thenReturn(saved);

        SavingsGoalResponse result = savingsService.createGoal(buildGoalRequest());

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getGoalName()).isEqualTo("Emergency Fund");
        verify(savingsDao).save(any(SavingsGoal.class));
    }

    @Test
    void getGoalsByMember_returnsList() {
        when(savingsDao.findByMemberId(1L)).thenReturn(List.of(buildGoal(1L), buildGoal(2L)));

        List<SavingsGoalResponse> goals = savingsService.getGoalsByMember(1L);

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
        when(expenseDao.findByMemberId(1L)).thenReturn(expenses);
        when(savingsDao.findByMemberId(1L)).thenReturn(List.of(buildGoal(1L)));

        SavingsRecommendationResponse response = savingsService.getRecommendations(1L);

        assertThat(response.getTotalExpenses()).isEqualByComparingTo("600.00");
        assertThat(response.getCurrentMonthlySavings()).isEqualByComparingTo("2400.00");
        assertThat(response.getRecommendations()).isNotEmpty();
        assertThat(response.getProjectedMonthsToGoal()).isNotNull();
    }
}
