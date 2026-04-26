package com.upkdev.financialtracker.domain.income.service;

import com.upkdev.financialtracker.domain.income.dto.*;
import java.util.List;

public interface IncomeService {
    IncomeResponse addIncome(IncomeRequest request);
    List<IncomeResponse> getIncomeForMember(Long memberId);
    List<IncomeResponse> getIncomeByCategory(Long memberId, String categoryCode);
    void deleteIncome(Long id);
}
