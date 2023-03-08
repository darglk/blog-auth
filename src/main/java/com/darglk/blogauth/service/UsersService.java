package com.darglk.blogauth.service;

import com.darglk.blogauth.rest.model.LoginResponse;
import com.darglk.blogcommons.model.LoginRequest;
import com.darglk.blogcommons.model.UserResponse;

public interface UsersService {
    LoginResponse login(LoginRequest loginRequest);

    UserResponse getUser(String id);
}
