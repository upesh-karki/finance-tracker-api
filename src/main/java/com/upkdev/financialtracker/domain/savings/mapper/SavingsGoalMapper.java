package com.upkdev.financialtracker.domain.savings.mapper;

import com.upkdev.financialtracker.domain.savings.dto.SavingsGoalRequest;
import com.upkdev.financialtracker.domain.savings.dto.SavingsGoalResponse;
import com.upkdev.financialtracker.domain.savings.entity.SavingsGoal;

public class SavingsGoalMapper {

    public static SavingsGoalResponse toResponse(SavingsGoal goal) {
        return SavingsGoalResponse.builder()
                .id(goal.getId())
                .memberId(goal.getMemberId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .currentAmount(goal.getCurrentAmount())
                .monthlySavingsTarget(goal.getMonthlySavingsTarget())
                .income(goal.getIncome())
                .targetDate(goal.getTargetDate())
                .status(goal.getStatus())
                .createdAt(goal.getCreatedAt())
                .build();
    }

    public static SavingsGoal toEntity(SavingsGoalRequest request) {
        return SavingsGoal.builder()
                .memberId(request.getMemberId())
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .income(request.getIncome())
                .monthlySavingsTarget(request.getMonthlySavingsTarget())
                .targetDate(request.getTargetDate())
                .build();
    }
}
