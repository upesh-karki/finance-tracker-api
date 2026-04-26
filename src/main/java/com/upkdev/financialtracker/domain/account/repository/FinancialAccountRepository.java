package com.upkdev.financialtracker.domain.account.repository;

import com.upkdev.financialtracker.domain.account.entity.FinancialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, Long> {
    List<FinancialAccount> findByMemberIdAndIsActiveTrue(Long memberId);
}
