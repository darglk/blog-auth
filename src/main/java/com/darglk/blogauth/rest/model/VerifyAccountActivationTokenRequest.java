package com.darglk.blogauth.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class VerifyAccountActivationTokenRequest {
    @NotBlank
    @Size(max = 36)
    private String token;
}
