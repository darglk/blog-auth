package com.darglk.blogauth.service;

import com.darglk.blogauth.rest.model.LoginResponse;
import com.darglk.blogauth.rest.model.SignupRequest;
import com.darglk.blogauth.rest.model.SignupResponse;
import com.darglk.blogcommons.model.LoginRequest;
import com.darglk.blogcommons.model.UserResponse;

public interface UsersService {
    LoginResponse login(LoginRequest loginRequest);

    UserResponse getUser(String id);

    SignupResponse signup(SignupRequest signupRequest);
}
