package com.upkdev.financialtracker.domain.expense.service.impl;

import com.upkdev.financialtracker.domain.expense.ExpenseCategory;
import com.upkdev.financialtracker.domain.expense.dao.ExpenseDao;
import com.upkdev.financialtracker.domain.expense.dto.ExpenseRequest;
import com.upkdev.financialtracker.domain.expense.dto.ExpenseResponse;
import com.upkdev.financialtracker.domain.expense.entity.Expense;
import com.upkdev.financialtracker.domain.expense.mapper.ExpenseMapper;
import com.upkdev.financialtracker.domain.expense.service.ExpenseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseDao expenseDao;

    @Override
    public ExpenseResponse create(ExpenseRequest request) {
        Expense expense = ExpenseMapper.toEntity(request);
        return ExpenseMapper.toResponse(expenseDao.save(expense));
    }

    @Override
    public List<ExpenseResponse> findByMember(Long memberId) {
        return expenseDao.findByMemberId(memberId).stream()
                .map(ExpenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseResponse> findByCategory(Long memberId, ExpenseCategory category) {
        return expenseDao.findByMemberIdAndCategory(memberId, category).stream()
                .map(ExpenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalByMember(Long memberId) {
        return expenseDao.findByMemberId(memberId).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalByCategory(Long memberId, ExpenseCategory category) {
        return expenseDao.findByMemberIdAndCategory(memberId, category).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void deleteById(Long id) {
        if (!expenseDao.existsById(id)) {
            throw new EntityNotFoundException("Expense not found with id: " + id);
        }
        expenseDao.deleteById(id);
    }
}
