package com.darglk.blogauth.rest;

import com.darglk.blogauth.rest.model.*;
import com.darglk.blogauth.service.UsersService;
import com.darglk.blogcommons.exception.ValidationException;
import com.darglk.blogcommons.model.LoginRequest;
import com.darglk.blogcommons.model.UserPrincipal;
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

    @DeleteMapping
    public void deleteAccount() {
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        usersService.deleteAccount(userId);
    }

    @PostMapping("/change-password")
    public LoginResponse changePassword(@RequestBody @Valid ChangePasswordRequest request, Errors errors) {
        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        }
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return usersService.changePassword(request, userId);
    }
}
