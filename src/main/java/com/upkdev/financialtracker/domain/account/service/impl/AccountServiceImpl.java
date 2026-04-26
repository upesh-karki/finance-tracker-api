package com.upkdev.financialtracker.domain.account.service.impl;

import com.upkdev.financialtracker.domain.account.dao.AccountDao;
import com.upkdev.financialtracker.domain.account.dto.*;
import com.upkdev.financialtracker.domain.account.entity.FinancialAccount;
import com.upkdev.financialtracker.domain.account.mapper.AccountMapper;
import com.upkdev.financialtracker.domain.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountDao accountDao;

    @Override
    public FinancialAccountResponse createAccount(FinancialAccountRequest request) {
        FinancialAccount account = AccountMapper.toEntity(request);
        return AccountMapper.toResponse(accountDao.save(account));
    }

    @Override
    public List<FinancialAccountResponse> getAccountsForMember(Long memberId) {
        return accountDao.findActiveByMemberId(memberId).stream()
                .map(a -> {
                    FinancialAccountResponse resp = AccountMapper.toResponse(a);
                    resp.setMissingMonths(getMissingMonths(a.getId()));
                    return resp;
                })
                .collect(Collectors.toList());
    }

    @Override
    public FinancialAccountResponse getAccount(Long accountId) {
        FinancialAccount account = accountDao.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));
        FinancialAccountResponse resp = AccountMapper.toResponse(account);
        resp.setMissingMonths(getMissingMonths(accountId));
        return resp;
    }

    @Override
    public void deactivateAccount(Long accountId) {
        accountDao.deactivate(accountId);
    }

    @Override
    public List<FinancialAccountResponse.MissingStatementMonth> getMissingMonths(Long accountId) {
        FinancialAccount account = accountDao.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));

        LocalDate start = account.getTrackingStartDate();
        LocalDate now = LocalDate.now();
        LocalDate lastMonth = now.withDayOfMonth(1).minusDays(1).withDayOfMonth(1);

        Set<String> uploaded = accountDao.findStatementsByAccountId(accountId).stream()
                .filter(s -> "UPLOADED".equals(s.getStatus()))
                .map(s -> s.getStatementYear() + "-" + s.getStatementMonth())
                .collect(Collectors.toSet());

        List<FinancialAccountResponse.MissingStatementMonth> missing = new ArrayList<>();
        LocalDate cursor = start.withDayOfMonth(1);
        while (!cursor.isAfter(lastMonth)) {
            String key = cursor.getYear() + "-" + cursor.getMonthValue();
            if (!uploaded.contains(key)) {
                missing.add(FinancialAccountResponse.MissingStatementMonth.builder()
                        .year(cursor.getYear())
                        .month(cursor.getMonthValue())
                        .monthLabel(cursor.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + cursor.getYear())
                        .build());
            }
            cursor = cursor.plusMonths(1);
        }
        return missing;
    }
}
