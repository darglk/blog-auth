package com.darglk.blogauth.rest;

import com.darglk.blogauth.rest.model.LoginResponse;
import com.darglk.blogauth.rest.model.RefreshTokenRequest;
import com.darglk.blogauth.rest.model.SignupRequest;
import com.darglk.blogauth.rest.model.SignupResponse;
import com.darglk.blogauth.service.UsersService;
import com.darglk.blogcommons.exception.ValidationException;
import com.darglk.blogcommons.model.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/api/v1/users")
@RestController
@RequiredArgsConstructor
public class UsersController {
    @Value("${keycloak.api.key}")
    private String jwtKey;

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

    @PostMapping("/signup")
    public SignupResponse signup(@RequestBody @Valid SignupRequest signupRequest, Errors errors) {
        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        }
        return usersService.signup(signupRequest);
    }

    @PostMapping("/logout")
    public void logout(@RequestParam(name = "all-sessions", defaultValue = "false") Boolean allSessions) {
        usersService.logout(allSessions);
    }

    @PostMapping("/refresh")
    public LoginResponse refreshToken(@RequestBody @Valid RefreshTokenRequest request, Errors errors) {
        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        }
        return usersService.refreshToken(request);
    }
}
