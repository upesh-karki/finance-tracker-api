package com.upkdev.financialtracker.domain.member;

import com.upkdev.financialtracker.domain.member.dao.MemberDao;
import com.upkdev.financialtracker.domain.member.dto.LoginRequest;
import com.upkdev.financialtracker.domain.member.dto.MemberRequest;
import com.upkdev.financialtracker.domain.member.dto.MemberResponse;
import com.upkdev.financialtracker.domain.member.entity.Member;
import com.upkdev.financialtracker.domain.member.service.impl.MemberServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberDao memberDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    private MemberRequest buildRequest() {
        MemberRequest req = new MemberRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@example.com");
        req.setPassword("pass");
        req.setUsername("johndoe");
        req.setOccupation("Engineer");
        req.setPhoneNumber("555-1234");
        return req;
    }

    private Member buildMember(Long id) {
        return Member.builder()
                .id(id)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("pass")
                .username("johndoe")
                .occupation("Engineer")
                .phoneNumber("555-1234")
                .profileStatus("ACTIVE")
                .build();
    }

    @Test
    void register_mapsAndReturnsResponse() {
        Member saved = buildMember(1L);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-pass");
        when(memberDao.save(any(Member.class))).thenReturn(saved);

        MemberResponse response = memberService.register(buildRequest());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getUsername()).isEqualTo("johndoe");
        verify(memberDao).save(any(Member.class));
    }

    @Test
    void findById_returnsResponse() {
        when(memberDao.findById(1L)).thenReturn(Optional.of(buildMember(1L)));

        MemberResponse response = memberService.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findById_notFound_throwsEntityNotFoundException() {
        when(memberDao.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteById_callsDao() {
        when(memberDao.existsById(1L)).thenReturn(true);

        memberService.deleteById(1L);

        verify(memberDao).deleteById(1L);
    }

    @Test
    void deleteById_notFound_throwsEntityNotFoundException() {
        when(memberDao.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> memberService.deleteById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void login_validCredentials_returnsResponse() {
        Member member = buildMember(1L);
        when(memberDao.findByUsername("johndoe")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("pass", "pass")).thenReturn(true);

        LoginRequest req = new LoginRequest();
        req.setUsername("johndoe");
        req.setPassword("pass");

        MemberResponse response = memberService.login(req);

        assertThat(response.getUsername()).isEqualTo("johndoe");
    }

    @Test
    void login_wrongPassword_throwsException() {
        Member member = buildMember(1L);
        when(memberDao.findByUsername("johndoe")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrongpass", "pass")).thenReturn(false);

        LoginRequest req = new LoginRequest();
        req.setUsername("johndoe");
        req.setPassword("wrongpass");

        assertThatThrownBy(() -> memberService.login(req))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAll_returnsList() {
        when(memberDao.findAll()).thenReturn(List.of(buildMember(1L), buildMember(2L)));

        List<MemberResponse> all = memberService.findAll();

        assertThat(all).hasSize(2);
    }
}
