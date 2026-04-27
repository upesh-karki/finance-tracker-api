package com.upkdev.financialtracker.domain.expense.mapper;

import com.upkdev.financialtracker.domain.expense.dto.ExpenseRequest;
import com.upkdev.financialtracker.domain.expense.dto.ExpenseResponse;
import com.upkdev.financialtracker.domain.expense.entity.Expense;

public class ExpenseMapper {

    public static ExpenseResponse toResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .memberId(expense.getMemberId())
                .accountId(expense.getAccountId())
                .statementId(expense.getStatementId())
                .institutionName(expense.getInstitutionName())
                .expenseName(expense.getExpenseName())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .expenseDate(expense.getExpenseDate())
                .createdAt(expense.getCreatedAt())
                .build();
    }

    public static Expense toEntity(ExpenseRequest request) {
        return Expense.builder()
                .memberId(request.getMemberId())
                .accountId(request.getAccountId())
                .statementId(request.getStatementId())
                .institutionName(request.getInstitutionName())
                .expenseName(request.getExpenseName())
                .amount(request.getAmount())
                .category(request.getCategory())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .build();
    }
}
