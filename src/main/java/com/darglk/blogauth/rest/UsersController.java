package com.darglk.blogauth.rest;

import com.darglk.blogauth.rest.model.LoginResponse;
import com.darglk.blogauth.service.UsersService;
import com.darglk.blogcommons.exception.ValidationException;
import com.darglk.blogcommons.model.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/api/v1/users")
@RestController
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

    @GetMapping("/current-user")
    public String currentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
    }
}
