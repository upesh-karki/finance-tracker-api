package com.upkdev.financialtracker.domain.account;

import com.upkdev.financialtracker.domain.account.dao.AccountDao;
import com.upkdev.financialtracker.domain.account.dto.FinancialAccountRequest;
import com.upkdev.financialtracker.domain.account.dto.FinancialAccountResponse;
import com.upkdev.financialtracker.domain.account.entity.AccountStatement;
import com.upkdev.financialtracker.domain.account.entity.FinancialAccount;
import com.upkdev.financialtracker.domain.account.service.impl.AccountServiceImpl;
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
class AccountServiceTest {

    @Mock
    private AccountDao accountDao;

    @InjectMocks
    private AccountServiceImpl accountService;

    private FinancialAccount buildAccount(Long id, Long memberId) {
        return FinancialAccount.builder()
                .id(id)
                .memberId(memberId)
                .nickname("Chequing")
                .institutionName("TD Bank")
                .accountTypeCode("CHEQUING")
                .trackingStartDate(LocalDate.now().minusMonths(2))
                .isActive(true)
                .build();
    }

    private FinancialAccountRequest buildRequest(Long memberId) {
        return FinancialAccountRequest.builder()
                .memberId(memberId)
                .nickname("Chequing")
                .institutionName("TD Bank")
                .accountTypeCode("CHEQUING")
                .openedDate(LocalDate.now().minusYears(1))
                .build();
    }

    @Test
    void createAccount_savesAndReturnsResponse() {
        FinancialAccount saved = buildAccount(1L, 1L);
        when(accountDao.save(any(FinancialAccount.class))).thenReturn(saved);

        FinancialAccountResponse response = accountService.createAccount(buildRequest(1L));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNickname()).isEqualTo("Chequing");
        assertThat(response.getInstitutionName()).isEqualTo("TD Bank");
        verify(accountDao).save(any(FinancialAccount.class));
    }

    @Test
    void getAccountsForMember_returnsMappedList() {
        FinancialAccount account = buildAccount(1L, 1L);
        when(accountDao.findActiveByMemberId(1L)).thenReturn(List.of(account));
        when(accountDao.findById(1L)).thenReturn(Optional.of(account));
        when(accountDao.findStatementsByAccountId(1L)).thenReturn(List.of());

        List<FinancialAccountResponse> result = accountService.getAccountsForMember(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMemberId()).isEqualTo(1L);
    }

    @Test
    void getAccount_notFound_throwsRuntimeException() {
        when(accountDao.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    void deactivateAccount_delegatesToDao() {
        doNothing().when(accountDao).deactivate(1L);

        accountService.deactivateAccount(1L);

        verify(accountDao).deactivate(1L);
    }

    @Test
    void getMissingMonths_allUploaded_returnsEmpty() {
        FinancialAccount account = buildAccount(1L, 1L);
        account.setTrackingStartDate(LocalDate.now().minusMonths(1).withDayOfMonth(1));

        AccountStatement stmt = AccountStatement.builder()
                .accountId(1L)
                .statementYear(LocalDate.now().minusMonths(1).getYear())
                .statementMonth(LocalDate.now().minusMonths(1).getMonthValue())
                .status("UPLOADED")
                .build();

        when(accountDao.findById(1L)).thenReturn(Optional.of(account));
        when(accountDao.findStatementsByAccountId(1L)).thenReturn(List.of(stmt));

        List<FinancialAccountResponse.MissingStatementMonth> missing = accountService.getMissingMonths(1L);

        assertThat(missing).isEmpty();
    }

    @Test
    void getMissingMonths_noneUploaded_returnsAllMonths() {
        FinancialAccount account = buildAccount(1L, 1L);
        account.setTrackingStartDate(LocalDate.now().minusMonths(3).withDayOfMonth(1));

        when(accountDao.findById(1L)).thenReturn(Optional.of(account));
        when(accountDao.findStatementsByAccountId(1L)).thenReturn(List.of());

        List<FinancialAccountResponse.MissingStatementMonth> missing = accountService.getMissingMonths(1L);

        assertThat(missing).hasSize(3);
    }
}
