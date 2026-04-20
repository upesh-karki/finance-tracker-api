package com.upkdev.financialtracker.domain.member.service.impl;

import com.upkdev.financialtracker.domain.member.dao.MemberDao;
import com.upkdev.financialtracker.domain.member.dto.LoginRequest;
import com.upkdev.financialtracker.domain.member.dto.MemberRequest;
import com.upkdev.financialtracker.domain.member.dto.MemberResponse;
import com.upkdev.financialtracker.domain.member.entity.Member;
import com.upkdev.financialtracker.domain.member.mapper.MemberMapper;
import com.upkdev.financialtracker.domain.member.service.MemberService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberDao memberDao;

    @Override
    public MemberResponse register(MemberRequest request) {
        Member member = MemberMapper.toEntity(request);
        Member saved = memberDao.save(member);
        return MemberMapper.toResponse(saved);
    }

    @Override
    public MemberResponse findById(Long id) {
        Member member = memberDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + id));
        return MemberMapper.toResponse(member);
    }

    @Override
    public List<MemberResponse> findAll() {
        return memberDao.findAll().stream()
                .map(MemberMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MemberResponse login(LoginRequest request) {
        Member member = memberDao.findByUsername(request.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Invalid username or password"));
        if (!member.getPassword().equals(request.getPassword())) {
            throw new EntityNotFoundException("Invalid username or password");
        }
        return MemberMapper.toResponse(member);
    }

    @Override
    public void deleteById(Long id) {
        if (!memberDao.existsById(id)) {
            throw new EntityNotFoundException("Member not found with id: " + id);
        }
        memberDao.deleteById(id);
    }
}
