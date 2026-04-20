package com.upkdev.financialtracker.domain.expense;

import com.upkdev.financialtracker.domain.member.Member;
import com.upkdev.financialtracker.domain.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Long memberId;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(Member.builder()
                .firstName("Test")
                .lastName("User")
                .email("testuser@example.com")
                .password("pass")
                .username("testuser")
                .build());
        memberId = member.getId();
    }

    private Expense buildExpense(ExpenseCategory category, BigDecimal amount, LocalDate date) {
        return Expense.builder()
                .memberId(memberId)
                .expenseName("Test Expense")
                .amount(amount)
                .category(category)
                .expenseDate(date)
                .build();
    }

    @Test
    void findByMemberId_returnsExpenses() {
        expenseRepository.save(buildExpense(ExpenseCategory.FOOD, new BigDecimal("50.00"), LocalDate.now()));
        expenseRepository.save(buildExpense(ExpenseCategory.TRANSPORT, new BigDecimal("30.00"), LocalDate.now()));

        List<Expense> expenses = expenseRepository.findByMemberId(memberId);
        assertThat(expenses).hasSize(2);
    }

    @Test
    void findByMemberIdAndCategory_returnsOnlyMatching() {
        expenseRepository.save(buildExpense(ExpenseCategory.FOOD, new BigDecimal("50.00"), LocalDate.now()));
        expenseRepository.save(buildExpense(ExpenseCategory.FOOD, new BigDecimal("20.00"), LocalDate.now()));
        expenseRepository.save(buildExpense(ExpenseCategory.TRANSPORT, new BigDecimal("30.00"), LocalDate.now()));

        List<Expense> food = expenseRepository.findByMemberIdAndCategory(memberId, ExpenseCategory.FOOD);
        assertThat(food).hasSize(2);
        assertThat(food).allMatch(e -> e.getCategory() == ExpenseCategory.FOOD);
    }

    @Test
    void findByMemberIdAndExpenseDateBetween_returnsInRange() {
        LocalDate today = LocalDate.now();
        expenseRepository.save(buildExpense(ExpenseCategory.FOOD, new BigDecimal("10.00"), today.minusDays(5)));
        expenseRepository.save(buildExpense(ExpenseCategory.FOOD, new BigDecimal("20.00"), today));
        expenseRepository.save(buildExpense(ExpenseCategory.FOOD, new BigDecimal("30.00"), today.plusDays(5)));

        List<Expense> inRange = expenseRepository.findByMemberIdAndExpenseDateBetween(
                memberId, today.minusDays(3), today.plusDays(3));
        assertThat(inRange).hasSize(1);
        assertThat(inRange.get(0).getAmount()).isEqualByComparingTo("20.00");
    }
}
