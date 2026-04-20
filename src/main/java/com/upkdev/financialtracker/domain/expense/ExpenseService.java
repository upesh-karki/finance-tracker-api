package com.upkdev.financialtracker.domain.expense;

import com.upkdev.financialtracker.domain.expense.dto.ExpenseRequest;
import com.upkdev.financialtracker.domain.expense.dto.ExpenseResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseResponse create(ExpenseRequest request) {
        Expense expense = Expense.builder()
                .memberId(request.getMemberId())
                .expenseName(request.getExpenseName())
                .amount(request.getAmount())
                .category(request.getCategory())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .build();
        return toResponse(expenseRepository.save(expense));
    }

    public List<ExpenseResponse> findByMember(Long memberId) {
        return expenseRepository.findByMemberId(memberId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ExpenseResponse> findByCategory(Long memberId, ExpenseCategory category) {
        return expenseRepository.findByMemberIdAndCategory(memberId, category).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BigDecimal getTotalByMember(Long memberId) {
        return expenseRepository.findByMemberId(memberId).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalByCategory(Long memberId, ExpenseCategory category) {
        return expenseRepository.findByMemberIdAndCategory(memberId, category).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void deleteById(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new EntityNotFoundException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }

    public ExpenseResponse toResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .memberId(expense.getMemberId())
                .expenseName(expense.getExpenseName())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .expenseDate(expense.getExpenseDate())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}
