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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberDao memberDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MemberResponse register(MemberRequest request) {
        Member member = MemberMapper.toEntity(request);
        member = Member.builder()
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .email(member.getEmail())
                .password(passwordEncoder.encode(member.getPassword()))
                .username(member.getUsername())
                .occupation(member.getOccupation())
                .phoneNumber(member.getPhoneNumber())
                .profileStatus(member.getProfileStatus())
                .build();
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
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
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
