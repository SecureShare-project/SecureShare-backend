package com.lakshith.secureshare.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lakshith.secureshare.model.PendingPasswordReset;
import com.lakshith.secureshare.model.PendingSignUp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, PendingSignUp> pendingSignupCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES) // generous outer bound — real 3.5-min OTP expiry is checked manually in AuthService
                .maximumSize(10_000)
                .build();
    }

    @Bean
    public Cache<String, PendingPasswordReset> pendingPasswordResetCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(10))
                .maximumSize(10_000)
                .build();
    }
}