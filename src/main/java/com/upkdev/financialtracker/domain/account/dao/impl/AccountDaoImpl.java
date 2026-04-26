package com.upkdev.financialtracker.domain.account.dao.impl;

import com.upkdev.financialtracker.domain.account.dao.AccountDao;
import com.upkdev.financialtracker.domain.account.entity.AccountStatement;
import com.upkdev.financialtracker.domain.account.entity.FinancialAccount;
import com.upkdev.financialtracker.domain.account.repository.AccountStatementRepository;
import com.upkdev.financialtracker.domain.account.repository.FinancialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountDaoImpl implements AccountDao {

    private final FinancialAccountRepository accountRepository;
    private final AccountStatementRepository statementRepository;

    @Override
    public FinancialAccount save(FinancialAccount account) {
        return accountRepository.save(account);
    }

    @Override
    public Optional<FinancialAccount> findById(Long id) {
        return accountRepository.findById(id);
    }

    @Override
    public List<FinancialAccount> findActiveByMemberId(Long memberId) {
        return accountRepository.findByMemberIdAndIsActiveTrue(memberId);
    }

    @Override
    public void deactivate(Long id) {
        accountRepository.findById(id).ifPresent(a -> {
            a.setIsActive(false);
            accountRepository.save(a);
        });
    }

    @Override
    public AccountStatement saveStatement(AccountStatement statement) {
        return statementRepository.save(statement);
    }

    @Override
    public List<AccountStatement> findStatementsByAccountId(Long accountId) {
        return statementRepository.findByAccountId(accountId);
    }

    @Override
    public Optional<AccountStatement> findStatement(Long accountId, int year, int month) {
        return statementRepository.findByAccountIdAndStatementYearAndStatementMonth(accountId, year, month);
    }
}
