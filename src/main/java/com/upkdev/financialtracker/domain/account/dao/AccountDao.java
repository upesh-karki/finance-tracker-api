package com.upkdev.financialtracker.domain.account.dao;

import com.upkdev.financialtracker.domain.account.entity.FinancialAccount;
import com.upkdev.financialtracker.domain.account.entity.AccountStatement;
import java.util.List;
import java.util.Optional;

public interface AccountDao {
    FinancialAccount save(FinancialAccount account);
    Optional<FinancialAccount> findById(Long id);
    List<FinancialAccount> findActiveByMemberId(Long memberId);
    void deactivate(Long id);
    AccountStatement saveStatement(AccountStatement statement);
    List<AccountStatement> findStatementsByAccountId(Long accountId);
    Optional<AccountStatement> findStatement(Long accountId, int year, int month);
}
