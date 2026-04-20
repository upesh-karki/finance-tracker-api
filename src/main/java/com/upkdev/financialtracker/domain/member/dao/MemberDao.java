package com.upkdev.financialtracker.domain.member.dao;

import com.upkdev.financialtracker.domain.member.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberDao {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByUsername(String username);
    List<Member> findAll();
    boolean existsById(Long id);
    void deleteById(Long id);
}
