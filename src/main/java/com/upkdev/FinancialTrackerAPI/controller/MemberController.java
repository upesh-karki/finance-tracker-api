package com.upkdev.FinancialTrackerAPI.controller;

import com.upkdev.FinancialTrackerAPI.entity.Member;
import com.upkdev.FinancialTrackerAPI.service.ExpenseService;
import com.upkdev.FinancialTrackerAPI.service.MemberRegistrationRequest;
import com.upkdev.FinancialTrackerAPI.service.MemberRowMapper;
import com.upkdev.FinancialTrackerAPI.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/members")
public class MemberController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public MemberController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/")
    public Member saveMember(@RequestBody Member reqClass) {
        if (reqClass.getMemberid() == null) {
            throw new IllegalArgumentException("Member ID cannot be null.");
        }
        return memberService.saveMember(reqClass);
    }

    @PostMapping("/register")
    public String registerMember(@RequestBody MemberRegistrationRequest request) {
        try {
            memberService.registerMember(request);
            return "Member registered successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error registering member: " + e.getMessage();
        }
    }

    @PostMapping("/login")
    public Member loginMember(@RequestBody Map<String, String> requestBody) {
        String userName = requestBody.get("userName");
        String password = requestBody.get("password");

        if (userName == null || password == null) {
            throw new IllegalArgumentException("Username and password must not be null.");
        }

        try {
            String sql = "SELECT * FROM ods.member WHERE userName = ? AND pass = ?";
            return jdbcTemplate.queryForObject(sql, new Object[]{userName, password}, new MemberRowMapper());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/{id}/expenses")
    public List<Map<String, Object>> getMemberExpenses(@PathVariable("id") Long memberId) {
        return expenseService.getMemberExpenses(memberId);
    }

    // Endpoint to add an expense for a member
    @PostMapping("/{id}/expenses")
    public String addExpense(@PathVariable("id") Long memberId, @RequestBody Map<String, String> expenseData) {
        return expenseService.addExpense(memberId, expenseData);
    }
}
