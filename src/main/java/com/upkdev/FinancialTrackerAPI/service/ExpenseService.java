package com.upkdev.FinancialTrackerAPI.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ExpenseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Method to add an expense for a member
    public String addExpense(Long memberId, Map<String, String> expenseData) {
        String expensename = expenseData.get("expensename");
        String expensecost = expenseData.get("expensecost");
        String recurring = expenseData.get("recurring");

        if (expensename == null || expensecost == null) {
            return "Expense name and cost are required.";
        }

        try {
            String sql = "INSERT INTO csid.expense (memberid, expensename, expensecost, recurring) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, memberId, expensename, expensecost, recurring);
            return "Expense added successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error adding expense: " + e.getMessage();
        }
    }

    // Method to retrieve member's expenses
    public List<Map<String, Object>> getMemberExpenses(Long memberId) {
        try {
            String sql = "SELECT expensename, expensecost, recurring FROM csid.expense WHERE memberid = ?";
            return jdbcTemplate.queryForList(sql, memberId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching expenses for member " + memberId);
        }
    }
}
