package com.upkdev.financialtracker.domain.savings;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavingsRepository extends JpaRepository<SavingsGoal, Long> {
    List<SavingsGoal> findByMemberId(Long memberId);
}
