package com.darglk.blogauth.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class VerifyPasswordResetTokenRequest {
    @NotBlank
    @Size(max = 36)
    private String token;
    @NotBlank
    @Size(min = 4, max = 100)
    private String newPassword;
}
