package com.upkdev.financialtracker.domain.expense.dao.impl;

import com.upkdev.financialtracker.domain.expense.ExpenseCategory;
import com.upkdev.financialtracker.domain.expense.dao.ExpenseDao;
import com.upkdev.financialtracker.domain.expense.entity.Expense;
import com.upkdev.financialtracker.domain.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ExpenseDaoImpl implements ExpenseDao {

    private final ExpenseRepository repository;

    @Override
    public Expense save(Expense expense) {
        return repository.save(expense);
    }

    @Override
    public Optional<Expense> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Expense> findByMemberId(Long memberId) {
        return repository.findByMemberId(memberId);
    }

    @Override
    public List<Expense> findByMemberIdAndCategory(Long memberId, ExpenseCategory category) {
        return repository.findByMemberIdAndCategory(memberId, category);
    }

    @Override
    public List<Expense> findByMemberIdAndExpenseDateBetween(Long memberId, LocalDate start, LocalDate end) {
        return repository.findByMemberIdAndExpenseDateBetween(memberId, start, end);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
