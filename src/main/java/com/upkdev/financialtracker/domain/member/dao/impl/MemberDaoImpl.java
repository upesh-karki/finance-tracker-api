package com.upkdev.financialtracker.domain.member.dao.impl;

import com.upkdev.financialtracker.domain.member.dao.MemberDao;
import com.upkdev.financialtracker.domain.member.entity.Member;
import com.upkdev.financialtracker.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberDaoImpl implements MemberDao {

    private final MemberRepository repository;

    @Override
    public Member save(Member member) {
        return repository.save(member);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    public List<Member> findAll() {
        return repository.findAll();
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
