package com.upkdev.financialtracker.domain.account.repository;

import com.upkdev.financialtracker.domain.account.entity.AccountStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccountStatementRepository extends JpaRepository<AccountStatement, Long> {
    List<AccountStatement> findByAccountId(Long accountId);
    Optional<AccountStatement> findByAccountIdAndStatementYearAndStatementMonth(Long accountId, int year, int month);
}
