package com.darglk.blogauth.rest;

import com.darglk.blogauth.BlogAuthApplication;
import com.darglk.blogauth.config.TestConfiguration;
import com.darglk.blogauth.connector.KeycloakConnector;
import com.darglk.blogauth.rest.model.KeycloakLoginResponse;
import com.darglk.blogcommons.model.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        classes = { BlogAuthApplication.class, TestConfiguration.class })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
public class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private KeycloakConnector keycloakConnector;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSignIn_success() throws Exception {
        var keycloakLoginResponse = new KeycloakLoginResponse();
        keycloakLoginResponse.setAccessToken("access_token");
        keycloakLoginResponse.setRefreshToken("refresh_token");
        when(keycloakConnector.signIn(any())).thenReturn(keycloakLoginResponse);
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("asdf123");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/login")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_token"));
    }

    @Test
    public void testSignIn_incorrectEmail() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("asdf123");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/login")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
