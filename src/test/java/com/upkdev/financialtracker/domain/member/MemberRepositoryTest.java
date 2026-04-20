package com.upkdev.financialtracker.domain.member;

import com.upkdev.financialtracker.domain.member.entity.Member;
import com.upkdev.financialtracker.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private Member buildMember(String email, String username) {
        return Member.builder()
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .password("secret")
                .username(username)
                .build();
    }

    @Test
    void saveAndFindById() {
        Member saved = memberRepository.save(buildMember("john@example.com", "johndoe"));
        assertThat(saved.getId()).isNotNull();

        Optional<Member> found = memberRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findByEmail_returnsMember() {
        memberRepository.save(buildMember("jane@example.com", "janedoe"));

        Optional<Member> found = memberRepository.findByEmail("jane@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("janedoe");
    }

    @Test
    void findByUsername_returnsMember() {
        memberRepository.save(buildMember("alice@example.com", "alice99"));

        Optional<Member> found = memberRepository.findByUsername("alice99");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void findByEmail_nonExistent_returnsEmpty() {
        Optional<Member> found = memberRepository.findByEmail("nobody@example.com");
        assertThat(found).isEmpty();
    }

    @Test
    void duplicateEmail_throwsException() {
        memberRepository.save(buildMember("dup@example.com", "user1"));
        Member duplicate = buildMember("dup@example.com", "user2");

        assertThatThrownBy(() -> {
            memberRepository.saveAndFlush(duplicate);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
