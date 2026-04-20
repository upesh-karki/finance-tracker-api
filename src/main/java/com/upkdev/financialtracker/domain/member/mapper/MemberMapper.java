package com.upkdev.financialtracker.domain.member.mapper;

import com.upkdev.financialtracker.domain.member.dto.MemberRequest;
import com.upkdev.financialtracker.domain.member.dto.MemberResponse;
import com.upkdev.financialtracker.domain.member.entity.Member;

public class MemberMapper {

    public static MemberResponse toResponse(Member member) {
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

    public static Member toEntity(MemberRequest request) {
        return Member.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .username(request.getUsername())
                .occupation(request.getOccupation())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }
}
