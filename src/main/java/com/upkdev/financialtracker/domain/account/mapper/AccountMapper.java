package com.upkdev.financialtracker.domain.account.mapper;

import com.upkdev.financialtracker.domain.account.dto.*;
import com.upkdev.financialtracker.domain.account.entity.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class AccountMapper {

    public static FinancialAccount toEntity(FinancialAccountRequest req) {
        LocalDate trackingStart = deriveTrackingStart(req.getOpenedDate());
        return FinancialAccount.builder()
                .memberId(req.getMemberId())
                .nickname(req.getNickname())
                .institutionName(req.getInstitutionName())
                .accountTypeCode(req.getAccountTypeCode())
                .openedDate(req.getOpenedDate())
                .trackingStartDate(trackingStart)
                .isActive(true)
                .build();
    }

    public static FinancialAccountResponse toResponse(FinancialAccount entity) {
        return FinancialAccountResponse.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .nickname(entity.getNickname())
                .institutionName(entity.getInstitutionName())
                .accountTypeCode(entity.getAccountTypeCode())
                .openedDate(entity.getOpenedDate())
                .trackingStartDate(entity.getTrackingStartDate())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static AccountStatementResponse toStatementResponse(AccountStatement entity) {
        return AccountStatementResponse.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .statementYear(entity.getStatementYear())
                .statementMonth(entity.getStatementMonth())
                .status(entity.getStatus())
                .fileName(entity.getFileName())
                .transactionCount(entity.getTransactionCount())
                .uploadedAt(entity.getUploadedAt())
                .build();
    }

    private static LocalDate deriveTrackingStart(LocalDate openedDate) {
        int currentYear = LocalDate.now().getYear();
        if (openedDate != null && openedDate.getYear() >= currentYear) {
            return openedDate.withDayOfMonth(1);
        }
        return LocalDate.of(currentYear, 1, 1);
    }

    public static String monthLabel(int month) {
        return Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }
}
