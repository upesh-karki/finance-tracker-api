package com.upkdev.financialtracker.domain.savings.repository;

import com.upkdev.financialtracker.domain.savings.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavingsRepository extends JpaRepository<SavingsGoal, Long> {
    List<SavingsGoal> findByMemberId(Long memberId);
}
