package com.darglk.blogauth.rest;

import com.darglk.blogauth.rest.model.LoginRequest;
import com.darglk.blogauth.rest.model.LoginResponse;
import com.darglk.blogauth.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return usersService.login(loginRequest);
    }
}
