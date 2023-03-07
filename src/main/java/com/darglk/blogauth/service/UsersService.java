package com.darglk.blogauth.service;

import com.darglk.blogauth.rest.model.LoginResponse;
import com.darglk.blogcommons.model.LoginRequest;

public interface UsersService {
    LoginResponse login(LoginRequest loginRequest);
}
