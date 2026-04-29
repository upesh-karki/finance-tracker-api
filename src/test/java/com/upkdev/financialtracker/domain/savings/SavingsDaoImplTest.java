package com.upkdev.financialtracker.domain.savings;

import com.upkdev.financialtracker.domain.savings.dao.impl.SavingsDaoImpl;
import com.upkdev.financialtracker.domain.savings.entity.SavingsGoal;
import com.upkdev.financialtracker.domain.savings.repository.SavingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsDaoImplTest {

    @Mock  SavingsRepository repository;
    @InjectMocks SavingsDaoImpl dao;

    private SavingsGoal buildGoal(Long id) {
        return SavingsGoal.builder().id(id).memberId(1L)
                .goalName("House").targetAmount(new BigDecimal("50000.00"))
                .status("ACTIVE").build();
    }

    @Test
    void save_delegates() {
        when(repository.save(any())).thenReturn(buildGoal(1L));
        assertThat(dao.save(buildGoal(null)).getId()).isEqualTo(1L);
    }

    @Test
    void findById_returnsPresent() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildGoal(1L)));
        assertThat(dao.findById(1L)).isPresent();
    }

    @Test
    void findById_missing_returnsEmpty() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThat(dao.findById(99L)).isEmpty();
    }

    @Test
    void findByMemberId_returnsList() {
        when(repository.findByMemberId(1L)).thenReturn(List.of(buildGoal(1L), buildGoal(2L)));
        assertThat(dao.findByMemberId(1L)).hasSize(2);
    }

    @Test
    void existsById_delegates() {
        when(repository.existsById(1L)).thenReturn(true);
        assertThat(dao.existsById(1L)).isTrue();
    }

    @Test
    void deleteById_delegates() {
        doNothing().when(repository).deleteById(1L);
        dao.deleteById(1L);
        verify(repository).deleteById(1L);
    }
}
