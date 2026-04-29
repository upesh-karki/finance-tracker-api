package com.upkdev.financialtracker.domain.member;

import com.upkdev.financialtracker.domain.member.dao.impl.MemberDaoImpl;
import com.upkdev.financialtracker.domain.member.entity.Member;
import com.upkdev.financialtracker.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberDaoImplTest {

    @Mock  MemberRepository repository;
    @InjectMocks MemberDaoImpl dao;

    private Member buildMember(Long id) {
        return Member.builder().id(id).firstName("John").lastName("Doe")
                .email("john@test.com").username("john@test.com")
                .password("hashed").emailVerified(true).build();
    }

    @Test
    void save_delegates() {
        when(repository.save(any())).thenReturn(buildMember(1L));
        assertThat(dao.save(buildMember(null)).getId()).isEqualTo(1L);
    }

    @Test
    void findById_present() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildMember(1L)));
        assertThat(dao.findById(1L)).isPresent();
    }

    @Test
    void findById_missing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThat(dao.findById(99L)).isEmpty();
    }

    @Test
    void findByEmail_present() {
        when(repository.findByEmail("john@test.com")).thenReturn(Optional.of(buildMember(1L)));
        assertThat(dao.findByEmail("john@test.com")).isPresent();
    }

    @Test
    void findByUsername_present() {
        when(repository.findByUsername("john@test.com")).thenReturn(Optional.of(buildMember(1L)));
        assertThat(dao.findByUsername("john@test.com")).isPresent();
    }

    @Test
    void findAll_returnsList() {
        when(repository.findAll()).thenReturn(List.of(buildMember(1L), buildMember(2L)));
        assertThat(dao.findAll()).hasSize(2);
    }

    @Test
    void existsById_returnsTrue() {
        when(repository.existsById(1L)).thenReturn(true);
        assertThat(dao.existsById(1L)).isTrue();
    }

    @Test
    void deleteById_delegates() {
        doNothing().when(repository).deleteById(1L);
        dao.deleteById(1L);
        verify(repository).deleteById(1L);
    }
}
