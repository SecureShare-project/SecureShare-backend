package com.lakshith.secureshare.repository;

import com.lakshith.secureshare.model.OtpAttempt;
import com.lakshith.secureshare.model.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface OtpAttemptRepository extends JpaRepository<OtpAttempt, Long> {
    long countByEmailAndSuccessfulFalseAndAttemptedAtAfter(String email, LocalDateTime after);

    long countByEmailAndPurposeAndSuccessfulFalseAndAttemptedAtAfter(
            String email, OtpPurpose purpose, LocalDateTime after);
}
