package com.darglk.blogauth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity webSecurity) {
        webSecurity.ignoring()
                .antMatchers("/api/v1/users/login");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/v1/users/login")
                .permitAll().and()
                .authorizeRequests()
                .anyRequest().authenticated();
//                .and()
//                .addFilter(new JwtAuthorizationFilter(authenticationManager()))
//                .addFilter(new JwtAuthenticationFilter(authenticationManager(), userRepository));

    }

    @Bean
    public AuthenticationManager authenticationManagerLoad() throws Exception {
        return authenticationManager();
    }
}
