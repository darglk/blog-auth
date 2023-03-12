package com.darglk.blogauth.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PasswordResetRequest {
    @NotBlank
    @Size(min = 4, max = 100)
    private String email;
}
