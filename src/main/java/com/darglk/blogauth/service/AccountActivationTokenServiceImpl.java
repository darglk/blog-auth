package com.darglk.blogauth.service;

import com.darglk.blogauth.repository.AccountActivationTokenRepository;
import com.darglk.blogauth.repository.UserRepository;
import com.darglk.blogauth.repository.entity.AccountActivationTokenEntity;
import com.darglk.blogcommons.events.Subjects;
import com.darglk.blogcommons.events.model.UserAccountActivatedEvent;
import com.darglk.blogcommons.events.model.UserDeletedEvent;
import com.darglk.blogcommons.exception.ErrorResponse;
import com.darglk.blogcommons.exception.NotFoundException;
import com.darglk.blogcommons.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountActivationTokenServiceImpl implements AccountActivationTokenService {

    @Value("${users.account-activation-token.expiration.hours}")
    private Long accountExpirationHours;
    private final AccountActivationTokenRepository accountActivationTokenRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public String generateToken(String userId) {
        accountActivationTokenRepository.deleteByUserId(userId);
        var token = new AccountActivationTokenEntity();
        token.setToken(UUID.randomUUID().toString());
        token.setId(UUID.randomUUID().toString());
        token.setCreatedAt(Instant.now());
        token.setUserId(userId);
        accountActivationTokenRepository.save(token);
        return token.getId();
    }

    @Override
    @Transactional(noRollbackFor = ValidationException.class)
    public void verifyToken(String tokenId, String token) {
        var accountActivationToken = accountActivationTokenRepository
                .findById(tokenId)
                .orElseThrow(() -> new NotFoundException("Not found token with id: " + tokenId));
        if (!accountActivationToken.getToken().equals(token)) {
            throw new ValidationException(List.of(new ErrorResponse("Tokens do not match", "token")));
        }
        var userId = accountActivationToken.getUserId();
        accountActivationTokenRepository.deleteByUserId(userId);
        if (accountActivationToken.getCreatedAt().plus(accountExpirationHours, HOURS).isBefore(Instant.now())) {
            rabbitTemplate.convertAndSend(Subjects.USER_DELETED_QUEUE, new UserDeletedEvent(userId));
            userRepository.deleteById(userId);
            throw new ValidationException(List.of(new ErrorResponse("Token is expired", "token")));
        }
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        user.setEnabled(true);
        userRepository.save(user);
        rabbitTemplate.convertAndSend(Subjects.USER_ACCOUNT_ACTIVATION_DONE_QUEUE, new UserAccountActivatedEvent(userId));
    }
}
