package com.darglk.blogauth.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/")
public class SampelController {

    @GetMapping("sampel")
    public String sampel() {
        return "HELLO";
    }
}
