package com.upkdev.financialtracker.domain.member;

import com.upkdev.financialtracker.domain.member.dto.LoginRequest;
import com.upkdev.financialtracker.domain.member.dto.MemberRequest;
import com.upkdev.financialtracker.domain.member.dto.MemberResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponse register(MemberRequest request) {
        Member member = Member.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .username(request.getUsername())
                .occupation(request.getOccupation())
                .phoneNumber(request.getPhoneNumber())
                .build();
        Member saved = memberRepository.save(member);
        return toResponse(saved);
    }

    public MemberResponse findById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + id));
        return toResponse(member);
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MemberResponse login(LoginRequest request) {
        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Invalid username or password"));
        if (!member.getPassword().equals(request.getPassword())) {
            throw new EntityNotFoundException("Invalid username or password");
        }
        return toResponse(member);
    }

    public void deleteById(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new EntityNotFoundException("Member not found with id: " + id);
        }
        memberRepository.deleteById(id);
    }

    private MemberResponse toResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .email(member.getEmail())
                .username(member.getUsername())
                .occupation(member.getOccupation())
                .phoneNumber(member.getPhoneNumber())
                .profileStatus(member.getProfileStatus())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
