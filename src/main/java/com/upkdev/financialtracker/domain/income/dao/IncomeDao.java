package com.upkdev.financialtracker.domain.income.dao;

import com.upkdev.financialtracker.domain.income.entity.Income;
import java.util.List;
import java.util.Optional;

public interface IncomeDao {
    Income save(Income income);
    Optional<Income> findById(Long id);
    List<Income> findByMemberId(Long memberId);
    List<Income> findByMemberIdAndCategory(Long memberId, String categoryCode);
    void delete(Long id);
}
