package com.darglk.blogauth.rest;

import com.darglk.blogauth.service.UsersService;
import com.darglk.blogcommons.model.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api-internal/users")
@RequiredArgsConstructor
public class UserControllerInternal {
    private final UsersService usersService;

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable("id") String id) {
        return usersService.getUser(id);
    }
}
