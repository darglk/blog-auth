package com.darglk.blogauth.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangeEmailRequest {
    @Email
    @NotBlank
    private String email;
}
