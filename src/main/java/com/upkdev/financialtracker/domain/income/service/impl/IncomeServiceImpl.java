package com.upkdev.financialtracker.domain.income.service.impl;

import com.upkdev.financialtracker.domain.income.dao.IncomeDao;
import com.upkdev.financialtracker.domain.income.dto.*;
import com.upkdev.financialtracker.domain.income.entity.Income;
import com.upkdev.financialtracker.domain.income.mapper.IncomeMapper;
import com.upkdev.financialtracker.domain.income.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeDao incomeDao;

    @Override
    public IncomeResponse addIncome(IncomeRequest request) {
        Income income = IncomeMapper.toEntity(request);
        return IncomeMapper.toResponse(incomeDao.save(income));
    }

    @Override
    public List<IncomeResponse> getIncomeForMember(Long memberId) {
        return incomeDao.findByMemberId(memberId).stream()
                .map(IncomeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<IncomeResponse> getIncomeByCategory(Long memberId, String categoryCode) {
        return incomeDao.findByMemberIdAndCategory(memberId, categoryCode).stream()
                .map(IncomeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteIncome(Long id) {
        incomeDao.delete(id);
    }
}
