package com.upkdev.financialtracker.domain.account.repository;

import com.upkdev.financialtracker.domain.account.entity.FinancialInstitution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FinancialInstitutionRepository extends JpaRepository<FinancialInstitution, String> {
    List<FinancialInstitution> findByIsActiveTrueOrderByName();
}
