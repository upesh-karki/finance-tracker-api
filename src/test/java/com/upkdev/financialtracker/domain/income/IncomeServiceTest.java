package com.upkdev.financialtracker.domain.income;

import com.upkdev.financialtracker.domain.income.dao.IncomeDao;
import com.upkdev.financialtracker.domain.income.dto.IncomeRequest;
import com.upkdev.financialtracker.domain.income.dto.IncomeResponse;
import com.upkdev.financialtracker.domain.income.entity.Income;
import com.upkdev.financialtracker.domain.income.service.impl.IncomeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncomeServiceTest {

    @Mock
    private IncomeDao incomeDao;

    @InjectMocks
    private IncomeServiceImpl incomeService;

    private Income buildIncome(Long id, Long memberId, BigDecimal amount) {
        return Income.builder()
                .id(id)
                .memberId(memberId)
                .sourceName("Employer")
                .amount(amount)
                .incomeCategoryCode("SALARY")
                .incomeDate(LocalDate.now())
                .build();
    }

    private IncomeRequest buildRequest(Long memberId) {
        return IncomeRequest.builder()
                .memberId(memberId)
                .sourceName("Employer")
                .amount(new BigDecimal("3000.00"))
                .incomeCategoryCode("SALARY")
                .incomeDate(LocalDate.now())
                .build();
    }

    @Test
    void addIncome_savesAndReturnsResponse() {
        Income saved = buildIncome(1L, 1L, new BigDecimal("3000.00"));
        when(incomeDao.save(any(Income.class))).thenReturn(saved);

        IncomeResponse response = incomeService.addIncome(buildRequest(1L));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getMemberId()).isEqualTo(1L);
        assertThat(response.getAmount()).isEqualByComparingTo("3000.00");
        verify(incomeDao).save(any(Income.class));
    }

    @Test
    void getIncomeForMember_returnsMappedList() {
        when(incomeDao.findByMemberId(1L)).thenReturn(List.of(
                buildIncome(1L, 1L, new BigDecimal("2000.00")),
                buildIncome(2L, 1L, new BigDecimal("500.00"))
        ));

        List<IncomeResponse> result = incomeService.getIncomeForMember(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("2000.00");
    }

    @Test
    void getIncomeForMember_emptyList_returnsEmpty() {
        when(incomeDao.findByMemberId(99L)).thenReturn(List.of());

        List<IncomeResponse> result = incomeService.getIncomeForMember(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void getIncomeByCategory_filtersByCategory() {
        when(incomeDao.findByMemberIdAndCategory(1L, "SALARY")).thenReturn(List.of(
                buildIncome(1L, 1L, new BigDecimal("3000.00"))
        ));

        List<IncomeResponse> result = incomeService.getIncomeByCategory(1L, "SALARY");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIncomeCategoryCode()).isEqualTo("SALARY");
    }

    @Test
    void deleteIncome_delegatesToDao() {
        doNothing().when(incomeDao).delete(5L);

        incomeService.deleteIncome(5L);

        verify(incomeDao).delete(5L);
    }
}
