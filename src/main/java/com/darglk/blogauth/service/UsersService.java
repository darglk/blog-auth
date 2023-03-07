package com.darglk.blogauth.service;

import com.darglk.blogauth.rest.model.LoginRequest;
import com.darglk.blogauth.rest.model.LoginResponse;

public interface UsersService {
    LoginResponse login(LoginRequest loginRequest);
}
