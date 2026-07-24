package com.ecommerce.userservice.service;

import com.ecommerce.userservice.config.JwtProperties;
import com.ecommerce.userservice.entity.RefreshToken;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.exception.InvalidTokenException;
import com.ecommerce.userservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public record RotatedToken(String rawToken, User user) {}

    @Transactional
    public String issue(User user){
        String rawToken = generateSecureToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiryDate(LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpiry())))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public RotatedToken rotate(String rawToken){
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (existing.isRevoked()){
            log.warn("Attempted reuse of revoked token for user: {}", existing.getUser().getEmail());
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (existing.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new InvalidTokenException("Refresh token has expired");
        }

        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        String newRawToken = issue(existing.getUser());
        return new RotatedToken(newRawToken, existing.getUser());
    }

    @Transactional
    public void revoke(String rawToken){
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    private String generateSecureToken(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hash(String token){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e){
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
