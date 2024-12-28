package com.upkdev.FinancialTrackerAPI.repository;

import com.upkdev.FinancialTrackerAPI.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
        Member findByMemberid(Long memberid);
        Member findByUserNameAndPassword(String userName, String password);
}
