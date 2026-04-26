package com.upkdev.financialtracker.domain.account.service;

import com.upkdev.financialtracker.domain.account.dto.*;
import java.util.List;

public interface AccountService {
    FinancialAccountResponse createAccount(FinancialAccountRequest request);
    List<FinancialAccountResponse> getAccountsForMember(Long memberId);
    FinancialAccountResponse getAccount(Long accountId);
    void deactivateAccount(Long accountId);
    List<FinancialAccountResponse.MissingStatementMonth> getMissingMonths(Long accountId);
}
