package com.upkdev.financialtracker.domain.member.service;

import com.upkdev.financialtracker.domain.member.dto.LoginRequest;
import com.upkdev.financialtracker.domain.member.dto.MemberRequest;
import com.upkdev.financialtracker.domain.member.dto.MemberResponse;

import java.util.List;

public interface MemberService {
    MemberResponse register(MemberRequest request);
    MemberResponse findById(Long id);
    List<MemberResponse> findAll();
    MemberResponse login(LoginRequest request);
    void deleteById(Long id);
}
