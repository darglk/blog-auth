package com.darglk.blogauth.rest;

import com.darglk.blogauth.rest.model.LoginResponse;
import com.darglk.blogauth.service.UsersService;
import com.darglk.blogcommons.exception.ValidationException;
import com.darglk.blogcommons.model.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest loginRequest, Errors errors) {
        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        }
        return usersService.login(loginRequest);
    }
}
