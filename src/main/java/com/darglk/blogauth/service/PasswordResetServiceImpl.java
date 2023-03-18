package com.darglk.blogauth.service;

import com.darglk.blogauth.connector.KeycloakRealm;
import com.darglk.blogauth.repository.PasswordResetTokenRepository;
import com.darglk.blogauth.repository.UserRepository;
import com.darglk.blogauth.repository.entity.PasswordResetTokenEntity;
import com.darglk.blogauth.rest.model.PasswordResetRequest;
import com.darglk.blogauth.rest.model.VerifyPasswordResetTokenRequest;
import com.darglk.blogcommons.exception.BadRequestException;
import com.darglk.blogcommons.exception.ErrorResponse;
import com.darglk.blogcommons.exception.NotFoundException;
import com.darglk.blogcommons.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.HOURS;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    @Value("${users.password-reset-token.expiration.hours}")
    private Long passwordResetTokenExpiration;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final KeycloakRealm keycloakRealm;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest passwordResetRequest) {
        var user = userRepository.findByEmail(passwordResetRequest.getEmail());
        if (user.isEmpty() || !user.get().getEnabled()) {
            log.warn("Attempt to reset password of non-existing or disabled user");
            return;
        }
        var userId = user.get().getId();
        passwordResetTokenRepository.deleteByUserId(userId);
        var passwordResetToken = new PasswordResetTokenEntity();
        passwordResetToken.setToken(UUID.randomUUID().toString());
        passwordResetToken.setId(UUID.randomUUID().toString());
        passwordResetToken.setUserId(userId);
        passwordResetToken.setCreatedAt(Instant.now());
        // TODO: send notification to provided email
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    @Transactional(noRollbackFor = { BadRequestException.class, ValidationException.class })
    public void verifyToken(VerifyPasswordResetTokenRequest verifyPasswordResetTokenRequest, String tokenId) {
        var token = verifyPasswordResetTokenRequest.getToken();
        var passwordResetTokenEntity = passwordResetTokenRepository
                .findById(tokenId)
                .orElseThrow(() -> new NotFoundException("Not found token with id: " + tokenId));
        if (!passwordResetTokenEntity.getToken().equals(token)) {
            throw new ValidationException(List.of(new ErrorResponse("Tokens do not match", "token")));
        }
        var userId = passwordResetTokenEntity.getUserId();
        passwordResetTokenRepository.deleteByUserId(userId);
        if (passwordResetTokenEntity.getCreatedAt().plus(passwordResetTokenExpiration, HOURS).isBefore(Instant.now())) {
            throw new ValidationException(List.of(new ErrorResponse("Token is expired", "token")));
        }
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        if (!user.getEnabled()) {
            throw new BadRequestException("User is not enabled");
        }
        user.setPasswordHash(passwordEncoder.encode(verifyPasswordResetTokenRequest.getNewPassword()));
        userRepository.save(user);
        keycloakRealm.updatePassword(userId, verifyPasswordResetTokenRequest.getNewPassword());
    }
}
