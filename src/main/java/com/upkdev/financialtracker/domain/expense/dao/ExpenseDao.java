package com.upkdev.financialtracker.domain.expense.dao;

import com.upkdev.financialtracker.domain.expense.ExpenseCategory;
import com.upkdev.financialtracker.domain.expense.entity.Expense;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseDao {
    Expense save(Expense expense);
    Optional<Expense> findById(Long id);
    List<Expense> findByMemberId(Long memberId);
    List<Expense> findByMemberIdAndCategory(Long memberId, ExpenseCategory category);
    List<Expense> findByMemberIdAndExpenseDateBetween(Long memberId, LocalDate start, LocalDate end);
    boolean existsById(Long id);
    void deleteById(Long id);
}
