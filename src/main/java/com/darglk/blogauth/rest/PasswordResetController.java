package com.darglk.blogauth.rest;

import com.darglk.blogauth.rest.model.PasswordResetRequest;
import com.darglk.blogauth.rest.model.VerifyPasswordResetTokenRequest;
import com.darglk.blogauth.service.PasswordResetService;
import com.darglk.blogcommons.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/api/v1/users/password-reset")
@RestController
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping
    public void resetPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest, Errors errors) {
        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        }
        passwordResetService.resetPassword(passwordResetRequest);
    }

    @PostMapping("/verify/{tokenId}")
    public void verifyToken(@PathVariable("tokenId") String tokenId, @RequestBody @Valid VerifyPasswordResetTokenRequest request, Errors errors) {
        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        }
        passwordResetService.verifyToken(request, tokenId);
    }
}
