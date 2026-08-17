package com.lakshith.secureshare.repository;

import com.lakshith.secureshare.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    long countByEmailAndSuccessfulFalseAndAttemptedAtAfter(String email, LocalDateTime after);

    long countByIpAddressAndSuccessfulFalseAndAttemptedAtAfter(String ipAddress, LocalDateTime after);
}