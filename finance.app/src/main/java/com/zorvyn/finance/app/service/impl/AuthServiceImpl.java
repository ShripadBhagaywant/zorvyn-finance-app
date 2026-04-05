package com.zorvyn.finance.app.service.impl;

import com.zorvyn.finance.app.dtos.request.UserLoginRequest;
import com.zorvyn.finance.app.dtos.response.UserLoginResponse;
import com.zorvyn.finance.app.dtos.response.UserRegisterResponse;
import com.zorvyn.finance.app.entity.BlackListedToken;
import com.zorvyn.finance.app.entity.User;
import com.zorvyn.finance.app.entity.enums.Status;
import com.zorvyn.finance.app.exception.AuthException;
import com.zorvyn.finance.app.exception.ResourceNotFoundException;
import com.zorvyn.finance.app.repository.BlackListedTokenRepository;
import com.zorvyn.finance.app.repository.UserRepository;
import com.zorvyn.finance.app.security.CookieService;
import com.zorvyn.finance.app.security.JwtService;
import com.zorvyn.finance.app.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final BlackListedTokenRepository blacklistRepo;

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Override
    @Transactional
    public UserLoginResponse login(UserLoginRequest request, HttpServletResponse response) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Login failed — user not found | email={}", request.email());
                    return new ResourceNotFoundException("Invalid credentials");
                });

        if(user.isDeleted() || user.getStatus() == Status.INACTIVE){
            log.warn("Login failed — account disabled | userId={} email={}", user.getId(), request.email());
            throw new AuthException("Account is disabled. Please contact support.");
        }

        if (user.isAccountLocked()) {
            if (user.isLockExpired()) {
                log.info("Account lock expired, resetting | userId={}", user.getId());
                user.setAccountLocked(false);
                user.setFailedAttempts(0);
            } else {
                log.warn("Login failed — account is locked | userId={}", user.getId());
                throw new AuthException("Account is locked. Please try again later.");
            }
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            handleFailedLogin(user);
            log.warn("Login failed — invalid password | userId={} failedAttempts={}",
                    user.getId(), user.getFailedAttempts());
            throw new AuthException("Invalid credentials");
        }

        resetFailedAttempts(user);
        String token = jwtService.generateToken(user);

        // 5. Attach to Cookie
        response.addHeader(HttpHeaders.SET_COOKIE, cookieService.createTokenCookie(token).toString());

        log.info("Login successful | userId={} email={} role={}",
                user.getId(), user.getEmail(), user.getRole());

        return UserLoginResponse.builder()
                .user(new UserRegisterResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(),user.getStatus()))
                .accessToken(token)
                .build();
    }

    @Override
    @Transactional
    public void logout(String token, HttpServletResponse response) {
        String jti = jwtService.extractJti(token);
        LocalDateTime expiry = jwtService.extractExpiration(token)
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        log.info("Logging out | jti={} expiresAt={}", jti, expiry);

        blacklistRepo.save(new BlackListedToken(null, jti, expiry));
        response.addHeader(HttpHeaders.SET_COOKIE, cookieService.deleteTokenCookie().toString());

        log.info("Logout successful — token blacklisted | jti={}", jti);

    }


    private void handleFailedLogin(User user) {
        user.setFailedAttempts(user.getFailedAttempts() + 1);
        if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setLockTime(LocalDateTime.now());

            log.warn("Account locked due to too many failed attempts | userId={} attempts={}",
                    user.getId(), user.getFailedAttempts());
        }
        userRepository.save(user);
    }

    private void resetFailedAttempts(User user) {
        user.setFailedAttempts(0);
        user.setAccountLocked(false);
        user.setLockTime(null);
        userRepository.save(user);
    }

}
