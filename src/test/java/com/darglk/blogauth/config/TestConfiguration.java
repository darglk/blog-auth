package com.darglk.blogauth.config;

import com.darglk.blogauth.connector.KeycloakConnector;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class TestConfiguration {

    @Bean
    @Profile("test")
    public KeycloakConnector keycloakConnector() {
        return Mockito.mock(KeycloakConnector.class);
    }
}
