package com.upkdev.financialtracker.domain.income.repository;

import com.upkdev.financialtracker.domain.income.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByMemberIdOrderByIncomeDateDesc(Long memberId);
    List<Income> findByMemberIdAndIncomeCategoryCode(Long memberId, String categoryCode);
}
