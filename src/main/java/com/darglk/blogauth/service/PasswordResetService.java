package com.darglk.blogauth.service;

import com.darglk.blogauth.rest.model.PasswordResetRequest;
import com.darglk.blogauth.rest.model.VerifyPasswordResetTokenRequest;

public interface PasswordResetService {
    void resetPassword(PasswordResetRequest passwordResetRequest);
    void verifyToken(VerifyPasswordResetTokenRequest verifyPasswordResetTokenRequest, String tokenId);
}
