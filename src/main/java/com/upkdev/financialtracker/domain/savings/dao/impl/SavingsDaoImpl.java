package com.upkdev.financialtracker.domain.savings.dao.impl;

import com.upkdev.financialtracker.domain.savings.dao.SavingsDao;
import com.upkdev.financialtracker.domain.savings.entity.SavingsGoal;
import com.upkdev.financialtracker.domain.savings.repository.SavingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SavingsDaoImpl implements SavingsDao {

    private final SavingsRepository repository;

    @Override
    public SavingsGoal save(SavingsGoal goal) {
        return repository.save(goal);
    }

    @Override
    public Optional<SavingsGoal> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<SavingsGoal> findByMemberId(Long memberId) {
        return repository.findByMemberId(memberId);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
