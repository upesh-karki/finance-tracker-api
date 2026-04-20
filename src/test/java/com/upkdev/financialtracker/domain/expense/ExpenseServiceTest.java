package com.upkdev.financialtracker.domain.expense;

import com.upkdev.financialtracker.domain.expense.dto.ExpenseRequest;
import com.upkdev.financialtracker.domain.expense.dto.ExpenseResponse;
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
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private Expense buildExpense(Long id, Long memberId, ExpenseCategory category, BigDecimal amount) {
        return Expense.builder()
                .id(id)
                .memberId(memberId)
                .expenseName("Test")
                .amount(amount)
                .category(category)
                .expenseDate(LocalDate.now())
                .build();
    }

    private ExpenseRequest buildRequest() {
        ExpenseRequest req = new ExpenseRequest();
        req.setMemberId(1L);
        req.setExpenseName("Groceries");
        req.setAmount(new BigDecimal("75.50"));
        req.setCategory(ExpenseCategory.FOOD);
        req.setExpenseDate(LocalDate.now());
        return req;
    }

    @Test
    void create_savesAndReturnsResponse() {
        Expense saved = buildExpense(1L, 1L, ExpenseCategory.FOOD, new BigDecimal("75.50"));
        when(expenseRepository.save(any(Expense.class))).thenReturn(saved);

        ExpenseResponse response = expenseService.create(buildRequest());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getMemberId()).isEqualTo(1L);
        assertThat(response.getAmount()).isEqualByComparingTo("75.50");
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void findByMember_returnsList() {
        when(expenseRepository.findByMemberId(1L)).thenReturn(List.of(
                buildExpense(1L, 1L, ExpenseCategory.FOOD, new BigDecimal("20.00")),
                buildExpense(2L, 1L, ExpenseCategory.TRANSPORT, new BigDecimal("30.00"))
        ));

        List<ExpenseResponse> result = expenseService.findByMember(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    void getTotalByMember_sumsCorrectly() {
        when(expenseRepository.findByMemberId(1L)).thenReturn(List.of(
                buildExpense(1L, 1L, ExpenseCategory.FOOD, new BigDecimal("50.00")),
                buildExpense(2L, 1L, ExpenseCategory.TRANSPORT, new BigDecimal("30.00"))
        ));

        BigDecimal total = expenseService.getTotalByMember(1L);

        assertThat(total).isEqualByComparingTo("80.00");
    }

    @Test
    void getTotalByCategory_sumsCorrectly() {
        when(expenseRepository.findByMemberIdAndCategory(1L, ExpenseCategory.FOOD)).thenReturn(List.of(
                buildExpense(1L, 1L, ExpenseCategory.FOOD, new BigDecimal("25.00")),
                buildExpense(2L, 1L, ExpenseCategory.FOOD, new BigDecimal("15.00"))
        ));

        BigDecimal total = expenseService.getTotalByCategory(1L, ExpenseCategory.FOOD);

        assertThat(total).isEqualByComparingTo("40.00");
    }
}
