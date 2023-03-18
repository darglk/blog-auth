package com.darglk.blogauth.service;

import com.darglk.blogauth.rest.model.*;
import com.darglk.blogcommons.model.LoginRequest;
import com.darglk.blogcommons.model.UserResponse;

public interface UsersService {
    LoginResponse login(LoginRequest loginRequest);

    UserResponse getUser(String id);

    SignupResponse signup(SignupRequest signupRequest);

    void logout(Boolean allSessions);

    LoginResponse refreshToken(RefreshTokenRequest request);

    void deleteAccount(String userId);

    LoginResponse changePassword(ChangePasswordRequest request, String userId);
}
