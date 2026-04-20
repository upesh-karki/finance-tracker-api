package com.upkdev.financialtracker.domain.expense.service;

import com.upkdev.financialtracker.domain.expense.ExpenseCategory;
import com.upkdev.financialtracker.domain.expense.dto.ExpenseRequest;
import com.upkdev.financialtracker.domain.expense.dto.ExpenseResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseService {
    ExpenseResponse create(ExpenseRequest request);
    List<ExpenseResponse> findByMember(Long memberId);
    List<ExpenseResponse> findByCategory(Long memberId, ExpenseCategory category);
    BigDecimal getTotalByMember(Long memberId);
    BigDecimal getTotalByCategory(Long memberId, ExpenseCategory category);
    void deleteById(Long id);
}
