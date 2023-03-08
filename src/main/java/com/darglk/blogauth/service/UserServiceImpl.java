package com.darglk.blogauth.service;

import com.darglk.blogauth.connector.KeycloakConnector;
import com.darglk.blogauth.repository.UserRepository;
import com.darglk.blogauth.rest.model.LoginResponse;
import com.darglk.blogcommons.exception.NotFoundException;
import com.darglk.blogcommons.model.AuthorityResponse;
import com.darglk.blogcommons.model.LoginRequest;
import com.darglk.blogcommons.model.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UsersService {

    private final KeycloakConnector keycloakConnector;
    private final UserRepository userRepository;

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
}
