package com.zorvyn.finance.app.service.impl;

import com.zorvyn.finance.app.repository.BlackListedTokenRepository;
import com.zorvyn.finance.app.service.TokenCleanupTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupTaskImpl implements TokenCleanupTask {

    private final BlackListedTokenRepository blackListedTokenRepository;


    @Scheduled(cron = "0 0 */12 * * *")
    @Transactional
    @Override
    public void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Starting expired token cleanup | threshold={}", now);

        blackListedTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());

        log.info("Expired token cleanup complete | threshold={}", now);
    }
}
