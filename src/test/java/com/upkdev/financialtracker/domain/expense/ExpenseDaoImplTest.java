package com.upkdev.financialtracker.domain.expense;

import com.upkdev.financialtracker.domain.expense.dao.impl.ExpenseDaoImpl;
import com.upkdev.financialtracker.domain.expense.entity.Expense;
import com.upkdev.financialtracker.domain.expense.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseDaoImplTest {

    @Mock  ExpenseRepository repository;
    @InjectMocks ExpenseDaoImpl dao;

    private Expense buildExpense(Long id) {
        return Expense.builder().id(id).memberId(1L)
                .expenseName("Coffee").amount(new BigDecimal("5.00"))
                .category(ExpenseCategory.FOOD).expenseDate(LocalDate.now()).build();
    }

    @Test
    void save_delegatesToRepository() {
        Expense e = buildExpense(1L);
        when(repository.save(any())).thenReturn(e);
        assertThat(dao.save(e).getId()).isEqualTo(1L);
    }

    @Test
    void findById_returnsOptional() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildExpense(1L)));
        assertThat(dao.findById(1L)).isPresent();
    }

    @Test
    void findById_missing_returnsEmpty() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThat(dao.findById(99L)).isEmpty();
    }

    @Test
    void findByMemberId_returnsList() {
        when(repository.findByMemberId(1L)).thenReturn(List.of(buildExpense(1L)));
        assertThat(dao.findByMemberId(1L)).hasSize(1);
    }

    @Test
    void findByMemberIdAndCategory_returnsList() {
        when(repository.findByMemberIdAndCategory(1L, ExpenseCategory.FOOD))
                .thenReturn(List.of(buildExpense(1L)));
        assertThat(dao.findByMemberIdAndCategory(1L, ExpenseCategory.FOOD)).hasSize(1);
    }

    @Test
    void findByMemberIdAndDateRange_returnsList() {
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end   = LocalDate.now();
        when(repository.findByMemberIdAndExpenseDateBetween(1L, start, end))
                .thenReturn(List.of(buildExpense(1L)));
        assertThat(dao.findByMemberIdAndExpenseDateBetween(1L, start, end)).hasSize(1);
    }

    @Test
    void existsById_delegates() {
        when(repository.existsById(1L)).thenReturn(true);
        assertThat(dao.existsById(1L)).isTrue();
    }

    @Test
    void deleteById_delegates() {
        doNothing().when(repository).deleteById(1L);
        dao.deleteById(1L);
        verify(repository).deleteById(1L);
    }
}
