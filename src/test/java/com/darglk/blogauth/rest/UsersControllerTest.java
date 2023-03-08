package com.darglk.blogauth.rest;

import com.darglk.blogauth.BlogAuthApplication;
import com.darglk.blogauth.config.TestConfiguration;
import com.darglk.blogauth.connector.KeycloakConnector;
import com.darglk.blogauth.repository.UserRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    @Autowired
    private UserRepository userRepository;

    private final String accessToken = "4a42f24d-208e-4e08-8f1f-51db0b960a4f:ROLE_USER,ROLE_ADMIN";
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].field").value("email"))
                .andExpect(jsonPath("$.errors.[0].message").value("may not be empty"));
    }

    @Test
    public void testCurrentUser_success() throws Exception {
        mockMvc.perform(request(HttpMethod.GET, "/api/v1/users/current-user")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
