package com.upkdev.financialtracker.domain.income;

import com.upkdev.financialtracker.domain.income.dao.impl.IncomeDaoImpl;
import com.upkdev.financialtracker.domain.income.entity.Income;
import com.upkdev.financialtracker.domain.income.repository.IncomeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncomeDaoImplTest {

    @Mock  IncomeRepository incomeRepository;
    @InjectMocks IncomeDaoImpl dao;

    private Income buildIncome(Long id) {
        return Income.builder().id(id).memberId(1L)
                .sourceName("Payroll").amount(new BigDecimal("3000.00"))
                .incomeCategoryCode("SALARY").incomeDate(LocalDate.now()).build();
    }

    @Test
    void save_delegates() {
        when(incomeRepository.save(any())).thenReturn(buildIncome(1L));
        assertThat(dao.save(buildIncome(null)).getId()).isEqualTo(1L);
    }

    @Test
    void findById_returnsPresent() {
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(buildIncome(1L)));
        assertThat(dao.findById(1L)).isPresent();
    }

    @Test
    void findById_missing_returnsEmpty() {
        when(incomeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(dao.findById(99L)).isEmpty();
    }

    @Test
    void findByMemberId_returnsList() {
        when(incomeRepository.findByMemberIdOrderByIncomeDateDesc(1L))
                .thenReturn(List.of(buildIncome(1L), buildIncome(2L)));
        assertThat(dao.findByMemberId(1L)).hasSize(2);
    }

    @Test
    void findByMemberIdAndCategory_filters() {
        when(incomeRepository.findByMemberIdAndIncomeCategoryCode(1L, "SALARY"))
                .thenReturn(List.of(buildIncome(1L)));
        assertThat(dao.findByMemberIdAndCategory(1L, "SALARY")).hasSize(1);
    }

    @Test
    void delete_delegates() {
        doNothing().when(incomeRepository).deleteById(1L);
        dao.delete(1L);
        verify(incomeRepository).deleteById(1L);
    }
}
