package com.upkdev.financialtracker.domain.savings.service;

import com.upkdev.financialtracker.domain.savings.dto.SavingsGoalRequest;
import com.upkdev.financialtracker.domain.savings.dto.SavingsGoalResponse;
import com.upkdev.financialtracker.domain.savings.dto.SavingsRecommendationResponse;

import java.util.List;

public interface SavingsService {
    SavingsGoalResponse createGoal(SavingsGoalRequest request);
    List<SavingsGoalResponse> getGoalsByMember(Long memberId);
    SavingsRecommendationResponse getRecommendations(Long memberId);
}
