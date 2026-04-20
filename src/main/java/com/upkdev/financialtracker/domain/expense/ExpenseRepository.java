package com.upkdev.financialtracker.domain.expense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByMemberId(Long memberId);
    List<Expense> findByMemberIdAndCategory(Long memberId, ExpenseCategory category);
    List<Expense> findByMemberIdAndExpenseDateBetween(Long memberId, LocalDate start, LocalDate end);
}
