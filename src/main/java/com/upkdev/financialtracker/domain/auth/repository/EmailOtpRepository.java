package com.upkdev.financialtracker.domain.auth.repository;

import com.upkdev.financialtracker.domain.auth.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findTopByMemberIdAndUsedFalseOrderByCreatedAtDesc(Long memberId);
    void deleteByMemberId(Long memberId);
}
