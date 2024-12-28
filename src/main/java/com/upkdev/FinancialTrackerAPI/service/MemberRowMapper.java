package com.upkdev.FinancialTrackerAPI.service;

import com.upkdev.FinancialTrackerAPI.entity.Member;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberRowMapper implements RowMapper<Member> {

    @Override
    public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
        Member member = new Member();
        member.setMemberid(rs.getLong("memberid"));
        member.setUserName(rs.getString("userName"));
        member.setPassword(rs.getString("pass"));
        member.setEmail(rs.getString("email"));
        member.setFirstName(rs.getString("firstName"));
        member.setLastName(rs.getString("lastName"));
        member.setProfileStatus(rs.getString("profileStatus"));
        return member;
    }
}
