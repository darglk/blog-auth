package com.darglk.blogauth.service;

import com.darglk.blogauth.connector.KeycloakConnector;
import com.darglk.blogauth.rest.model.LoginRequest;
import com.darglk.blogauth.rest.model.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UsersService {

    private final KeycloakConnector keycloakConnector;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        var kcResponse = keycloakConnector.signIn(loginRequest);
        var loginResponse = new LoginResponse();
        loginResponse.setRefreshToken(kcResponse.getRefreshToken());
        loginResponse.setAccessToken(kcResponse.getAccessToken());

        return loginResponse;
    }
}
