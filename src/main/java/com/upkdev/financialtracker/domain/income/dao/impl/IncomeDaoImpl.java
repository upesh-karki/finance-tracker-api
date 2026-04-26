package com.upkdev.financialtracker.domain.income.dao.impl;

import com.upkdev.financialtracker.domain.income.dao.IncomeDao;
import com.upkdev.financialtracker.domain.income.entity.Income;
import com.upkdev.financialtracker.domain.income.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IncomeDaoImpl implements IncomeDao {

    private final IncomeRepository incomeRepository;

    @Override
    public Income save(Income income) { return incomeRepository.save(income); }

    @Override
    public Optional<Income> findById(Long id) { return incomeRepository.findById(id); }

    @Override
    public List<Income> findByMemberId(Long memberId) {
        return incomeRepository.findByMemberIdOrderByIncomeDateDesc(memberId);
    }

    @Override
    public List<Income> findByMemberIdAndCategory(Long memberId, String categoryCode) {
        return incomeRepository.findByMemberIdAndIncomeCategoryCode(memberId, categoryCode);
    }

    @Override
    public void delete(Long id) { incomeRepository.deleteById(id); }
}
