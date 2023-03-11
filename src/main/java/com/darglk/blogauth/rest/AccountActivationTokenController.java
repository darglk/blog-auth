package com.darglk.blogauth.rest;

import com.darglk.blogauth.rest.model.VerifyAccountActivationTokenRequest;
import com.darglk.blogauth.service.AccountActivationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/users/token")
@RestController
@RequiredArgsConstructor
public class AccountActivationTokenController {

    private final AccountActivationTokenService accountActivationTokenService;

    @PostMapping("/{tokenId}")
    public void verifyToken(@PathVariable("tokenId") String tokenId,
                            @RequestBody VerifyAccountActivationTokenRequest request) {
        accountActivationTokenService.verifyToken(tokenId, request.getToken());
    }
}
