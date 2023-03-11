package com.darglk.blogauth.service;

public interface AccountActivationTokenService {
    String generateToken(String userId);
    void verifyToken(String tokenId, String token);
}
