package com.upkdev.financialtracker.domain.member.repository;

import com.upkdev.financialtracker.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByUsername(String username);
}
