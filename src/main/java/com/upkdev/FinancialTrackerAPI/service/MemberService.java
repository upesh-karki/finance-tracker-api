package com.upkdev.FinancialTrackerAPI.service;

import com.upkdev.FinancialTrackerAPI.entity.Member;
import com.upkdev.FinancialTrackerAPI.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Member saveMember(Member dbRequest) {
        return memberRepository.save(dbRequest);
    }

    public void registerMember(MemberRegistrationRequest request) {
        jdbcTemplate.update(
                "EXEC RegisterUser ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?",
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                request.getOccupation(),
                request.getAddress1(),
                request.getAddress2(),
                request.getCity(),
                request.getCountry(),
                request.getZipcode(),
                request.getPhoneNumber(),
                request.getUserName(),
                request.getProfileStatus()
        );
    }

    public Member findMemberById(Long memberId) {
        String sql = "SELECT * FROM ods.member WHERE memberid = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{memberId}, new MemberRowMapper());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
