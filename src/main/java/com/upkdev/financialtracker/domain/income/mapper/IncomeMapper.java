package com.upkdev.financialtracker.domain.income.mapper;

import com.upkdev.financialtracker.domain.income.dto.*;
import com.upkdev.financialtracker.domain.income.entity.Income;

public class IncomeMapper {

    public static Income toEntity(IncomeRequest req) {
        return Income.builder()
                .memberId(req.getMemberId())
                .accountId(req.getAccountId())
                .statementId(req.getStatementId())
                .sourceName(req.getSourceName())
                .institutionName(req.getInstitutionName())
                .amount(req.getAmount())
                .incomeCategoryCode(req.getIncomeCategoryCode())
                .incomeDate(req.getIncomeDate())
                .description(req.getDescription())
                .build();
    }

    public static IncomeResponse toResponse(Income entity) {
        return IncomeResponse.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .accountId(entity.getAccountId())
                .statementId(entity.getStatementId())
                .sourceName(entity.getSourceName())
                .institutionName(entity.getInstitutionName())
                .amount(entity.getAmount())
                .incomeCategoryCode(entity.getIncomeCategoryCode())
                .incomeDate(entity.getIncomeDate())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
