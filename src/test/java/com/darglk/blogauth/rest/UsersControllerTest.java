package com.darglk.blogauth.rest;

import com.darglk.blogauth.BlogAuthApplication;
import com.darglk.blogauth.config.TestConfiguration;
import com.darglk.blogauth.connector.KeycloakConnector;
import com.darglk.blogauth.connector.KeycloakRealm;
import com.darglk.blogauth.repository.AccountActivationTokenRepository;
import com.darglk.blogauth.repository.AuthorityRepository;
import com.darglk.blogauth.repository.UserRepository;
import com.darglk.blogauth.repository.entity.AuthorityEntity;
import com.darglk.blogauth.repository.entity.UserEntity;
import com.darglk.blogauth.rest.model.ChangeEmailRequest;
import com.darglk.blogauth.rest.model.ChangePasswordRequest;
import com.darglk.blogauth.rest.model.KeycloakLoginResponse;
import com.darglk.blogauth.rest.model.RefreshTokenRequest;
import com.darglk.blogcommons.model.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        classes = { BlogAuthApplication.class, TestConfiguration.class })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private KeycloakConnector keycloakConnector;
    @Autowired
    private KeycloakRealm keycloakRealm;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private AccountActivationTokenRepository accountActivationTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String accessToken = "4a42f24d-208e-4e08-8f1f-51db0b960a4f:ROLE_USER,ROLE_ADMIN";
    private ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    public void teardown() {
        accountActivationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

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

    @Test
    public void testSignUp_incorrectEmail() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("asdf123");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/signup")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].field").value("email"))
                .andExpect(jsonPath("$.errors.[0].message").value("must not be blank"));

        request.setEmail("testing.incorerectemail");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/signup")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].field").value("email"))
                .andExpect(jsonPath("$.errors.[0].message").value("must be a well-formed email address"));
    }

    @Test
    public void testSignUp_incorrectPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("testing@test.com");
        request.setPassword("");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/signup")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(2)))
                .andExpect(jsonPath("$.errors.[0].field").value("password"))
                .andExpect(jsonPath("$.errors.[1].field").value("password"))
                .andExpect(jsonPath("$.errors.[*].message").value(Matchers.containsInAnyOrder("size must be between 4 and 100", "must not be blank")));
    }

    @Test
    public void testSignUp_emailExists() throws Exception {
        createUser();
        LoginRequest request = new LoginRequest();
        request.setEmail("testing@test.com");
        request.setPassword("asdf1234");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/signup")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].field").value("email"))
                .andExpect(jsonPath("$.errors.[0].message").value("email is taken"));
    }

    @Test
    public void testSignUp() throws Exception {
        var userId = "user_id";
        when(keycloakRealm.createUser(any())).thenReturn(userId);
        LoginRequest request = new LoginRequest();
        request.setEmail("testing@test.com");
        request.setPassword("asdf1234");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/signup")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));

        var user = userRepository.findById(userId);
        var token = accountActivationTokenRepository.findByUserId(userId);
        assertTrue(user.isPresent());
        assertFalse(user.get().getEnabled());
        assertTrue(token.isPresent());
        verify(keycloakRealm, times(1)).createUser(any());
    }

    @Test
    public void logout_singleSession() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(keycloakRealm, times(1)).logout(any());
    }

    @Test
    public void logout_allSessions() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/logout?all-sessions=true")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(keycloakRealm, times(1)).logoutAllSessions(any());
    }

    @Test
    public void refreshToken() throws Exception {
        var keycloakLoginResponse = new KeycloakLoginResponse();
        keycloakLoginResponse.setAccessToken("access_token");
        keycloakLoginResponse.setRefreshToken("refresh_token");

        when(keycloakConnector.refreshToken("refresh_token")).thenReturn(keycloakLoginResponse);
        var refreshToken = new RefreshTokenRequest("refresh_token");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/refresh")
                        .content(objectMapper.writeValueAsString(refreshToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_token"));
        verify(keycloakConnector, times(1)).refreshToken("refresh_token");
    }

    @Test
    public void deleteAccount() throws Exception {
        createUser();
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/users")
                        .header("Authorization", "Bearer user_id:ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        var user = userRepository.findById("user_id");
        assertTrue(user.isEmpty());
        verify(keycloakRealm, times(1)).deleteUser("user_id");
    }

    @Test
    public void changePassword_oldPasswordIncorrect() throws Exception {
        createUser();
        var request = new ChangePasswordRequest("asdf123", "afda3456");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/change-password")
                        .header("Authorization", "Bearer user_id:ROLE_USER")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].field").value("oldPassword"))
                .andExpect(jsonPath("$.errors.[0].message").value("Old password is incorrect"));
    }

    @Test
    public void changePassword() throws Exception {
        var keycloakLoginResponse = new KeycloakLoginResponse();
        keycloakLoginResponse.setAccessToken("access_token");
        keycloakLoginResponse.setRefreshToken("refresh_token");
        when(keycloakConnector.signIn(any())).thenReturn(keycloakLoginResponse);
        createUser();
        var request = new ChangePasswordRequest("asdf1234", "afda3456");
        var oldHash = userRepository.findById("user_id").get().getPasswordHash();
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/change-password")
                        .header("Authorization", "Bearer user_id:ROLE_USER")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_token"));

        var user = userRepository.findById("user_id");
        verify(keycloakRealm, times(1)).logoutAllSessions("user_id");
        verify(keycloakRealm, times(1)).updatePassword("user_id", "afda3456");
        verify(keycloakConnector, times(1)).signIn(any());
        assertNotEquals(oldHash, user.get().getPasswordHash());
    }

    @Test
    public void changeEmail_theSameEmail() throws Exception {
        createUser();
        var request = new ChangeEmailRequest("testing@test.com");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/change-email")
                        .header("Authorization", "Bearer user_id:ROLE_USER")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].field").value("email"))
                .andExpect(jsonPath("$.errors.[0].message").value("email is the same"));
    }

    @Test
    public void changeEmail_emailTaken() throws Exception {
        createUser();
        createUser("second_user_id", "test2@test.com");
        var request = new ChangeEmailRequest("test2@test.com");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/change-email")
                        .header("Authorization", "Bearer user_id:ROLE_USER")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].field").value("email"))
                .andExpect(jsonPath("$.errors.[0].message").value("email is taken"));
    }

    @Test
    public void changeEmail() throws Exception {
        createUser();
        var request = new ChangeEmailRequest("test3@test.com");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/change-email")
                        .header("Authorization", "Bearer user_id:ROLE_USER")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        var user = userRepository.findById("user_id").get();
        assertEquals("test3@test.com", user.getEmail());
        verify(keycloakRealm, times(1)).updateEmail("test3@test.com", "user_id");
    }

    private void createUser(String userId, String email) {
        var authority = new AuthorityEntity();
        authority.setId("ROLE_USER");
        authority.setName("ROLE_USER");
        var user = new UserEntity();
        user.setEmail(email);
        user.setId(userId);
        user.setPasswordHash(passwordEncoder.encode("asdf1234"));
        user.setEnabled(true);
        user.setAuthorities(List.of(authority));
        userRepository.save(user);
    }

    private void createUser() {
        createUser("user_id", "testing@test.com");
    }
}
