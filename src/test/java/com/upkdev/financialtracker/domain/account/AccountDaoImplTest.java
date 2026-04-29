package com.upkdev.financialtracker.domain.account;

import com.upkdev.financialtracker.domain.account.dao.impl.AccountDaoImpl;
import com.upkdev.financialtracker.domain.account.entity.AccountStatement;
import com.upkdev.financialtracker.domain.account.entity.FinancialAccount;
import com.upkdev.financialtracker.domain.account.repository.AccountStatementRepository;
import com.upkdev.financialtracker.domain.account.repository.FinancialAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountDaoImplTest {

    @Mock  FinancialAccountRepository accountRepository;
    @Mock  AccountStatementRepository statementRepository;
    @InjectMocks AccountDaoImpl dao;

    private FinancialAccount buildAccount(Long id) {
        return FinancialAccount.builder().id(id).memberId(1L)
                .nickname("Chequing").institutionName("TD Bank")
                .accountTypeCode("CHEQUING")
                .trackingStartDate(LocalDate.now().minusMonths(1))
                .isActive(true).build();
    }

    private AccountStatement buildStatement(Long id, Long accountId) {
        return AccountStatement.builder().id(id).accountId(accountId)
                .statementYear(2024).statementMonth(1).status("UPLOADED").build();
    }

    @Test
    void save_delegates() {
        when(accountRepository.save(any())).thenReturn(buildAccount(1L));
        assertThat(dao.save(buildAccount(null)).getId()).isEqualTo(1L);
    }

    @Test
    void findById_present() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(buildAccount(1L)));
        assertThat(dao.findById(1L)).isPresent();
    }

    @Test
    void findById_missing_empty() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(dao.findById(99L)).isEmpty();
    }

    @Test
    void findActiveByMemberId_returnsList() {
        when(accountRepository.findByMemberIdAndIsActiveTrue(1L))
                .thenReturn(List.of(buildAccount(1L)));
        assertThat(dao.findActiveByMemberId(1L)).hasSize(1);
    }

    @Test
    void deactivate_setsIsActiveFalse() {
        FinancialAccount account = buildAccount(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        dao.deactivate(1L);

        assertThat(account.getIsActive()).isFalse();
        verify(accountRepository).save(account);
    }

    @Test
    void deactivate_notFound_doesNothing() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());
        dao.deactivate(99L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void saveStatement_delegates() {
        AccountStatement stmt = buildStatement(1L, 1L);
        when(statementRepository.save(any())).thenReturn(stmt);
        assertThat(dao.saveStatement(stmt).getId()).isEqualTo(1L);
    }

    @Test
    void findStatementsByAccountId_returnsList() {
        when(statementRepository.findByAccountId(1L))
                .thenReturn(List.of(buildStatement(1L, 1L)));
        assertThat(dao.findStatementsByAccountId(1L)).hasSize(1);
    }

    @Test
    void findStatement_returnsOptional() {
        when(statementRepository.findByAccountIdAndStatementYearAndStatementMonth(1L, 2024, 1))
                .thenReturn(Optional.of(buildStatement(1L, 1L)));
        assertThat(dao.findStatement(1L, 2024, 1)).isPresent();
    }

    @Test
    void findStatement_missing_returnsEmpty() {
        when(statementRepository.findByAccountIdAndStatementYearAndStatementMonth(1L, 2020, 1))
                .thenReturn(Optional.empty());
        assertThat(dao.findStatement(1L, 2020, 1)).isEmpty();
    }
}
