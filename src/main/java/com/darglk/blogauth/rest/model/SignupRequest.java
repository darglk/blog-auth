package com.darglk.blogauth.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SignupRequest {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    @Size(min = 4, max = 100)
    private String password;
}
