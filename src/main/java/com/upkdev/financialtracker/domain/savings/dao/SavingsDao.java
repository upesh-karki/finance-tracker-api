package com.upkdev.financialtracker.domain.savings.dao;

import com.upkdev.financialtracker.domain.savings.entity.SavingsGoal;

import java.util.List;
import java.util.Optional;

public interface SavingsDao {
    SavingsGoal save(SavingsGoal goal);
    Optional<SavingsGoal> findById(Long id);
    List<SavingsGoal> findByMemberId(Long memberId);
    boolean existsById(Long id);
    void deleteById(Long id);
}
