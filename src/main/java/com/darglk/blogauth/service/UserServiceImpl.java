package com.darglk.blogauth.service;

import com.darglk.blogauth.connector.KeycloakConnector;
import com.darglk.blogauth.connector.KeycloakRealm;
import com.darglk.blogauth.repository.AuthorityRepository;
import com.darglk.blogauth.repository.UserRepository;
import com.darglk.blogauth.repository.entity.UserEntity;
import com.darglk.blogauth.rest.model.*;
import com.darglk.blogcommons.events.Subjects;
import com.darglk.blogcommons.exception.ErrorResponse;
import com.darglk.blogcommons.exception.NotFoundException;
import com.darglk.blogcommons.exception.ValidationException;
import com.darglk.blogcommons.model.AuthorityResponse;
import com.darglk.blogcommons.model.LoginRequest;
import com.darglk.blogcommons.model.UserPrincipal;
import com.darglk.blogcommons.model.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UsersService {

    private final KeycloakConnector keycloakConnector;
    private final KeycloakRealm realm;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final RabbitTemplate rabbitTemplate;
    private final AccountActivationTokenService accountActivationTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        var kcResponse = keycloakConnector.signIn(loginRequest);
        var loginResponse = new LoginResponse();
        loginResponse.setRefreshToken(kcResponse.getRefreshToken());
        loginResponse.setAccessToken(kcResponse.getAccessToken());

        return loginResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(String id) {
        var userEntity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id: " + id + " was not found"));

        var userResponse = new UserResponse();
        userResponse.setEmail(userEntity.getEmail());
        userResponse.setId(userEntity.getId());
        userResponse.setEnabled(userEntity.getEnabled());
        userResponse.setAuthorities(userEntity.getAuthorities()
                .stream()
                .map(authority ->
                    new AuthorityResponse(authority.getId(), authority.getName())
                )
                .collect(Collectors.toList()));

        return userResponse;
    }

    @Override
    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {
        userRepository.findByEmail(signupRequest.getEmail()).ifPresent(user -> {
            throw new ValidationException(List.of(new ErrorResponse("email is taken", "email")));
        });
        var userId = realm.createUser(signupRequest);
        var userAuthority = authorityRepository.findById("ROLE_USER").get();
        var newUser = new UserEntity();
        newUser.setAuthorities(List.of(userAuthority));
        newUser.setEmail(signupRequest.getEmail());
        newUser.setEnabled(false);
        newUser.setId(userId);
        userRepository.save(newUser);
        var token = accountActivationTokenService.generateToken(userId);
        // TODO: pass token to message
        rabbitTemplate.convertAndSend(Subjects.UserCreated.getSubject(), userId);

        return new SignupResponse(userId);
    }

    @Override
    public void logout(Boolean allSessions) {
        var currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (allSessions) {
            realm.logoutAllSessions(currentUser.getId());
        } else {
            realm.logout(currentUser.getSessionId());
        }
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        var response = keycloakConnector.refreshToken(request.getRefreshToken());
        var loginResponse = new LoginResponse();
        loginResponse.setAccessToken(response.getAccessToken());
        loginResponse.setRefreshToken(response.getRefreshToken());
        return loginResponse;
    }

    @Override
    @Transactional
    public void deleteAccount(String userId) {
        userRepository.deleteById(userId);
        realm.deleteUser(userId);
        // TODO: send message to queue
    }

    @Override
    @Transactional
    public LoginResponse changePassword(ChangePasswordRequest request, String userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id: " + userId + " was not found"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new ValidationException(List.of(new ErrorResponse("Old password is incorrect", "oldPassword")));
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        realm.updatePassword(userId, request.getNewPassword());
        realm.logoutAllSessions(userId);
        var loginRequest = new LoginRequest();
        loginRequest.setEmail(user.getEmail());
        loginRequest.setPassword(request.getNewPassword());
        // TODO: notify user about new password
        return login(loginRequest);
    }

    @RabbitListener(queues = "user_created")
    public void listen(String in) {
        System.out.println(in);
    }
}
