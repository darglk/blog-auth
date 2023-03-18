package com.darglk.blogauth.rest;

import com.darglk.blogauth.BlogAuthApplication;
import com.darglk.blogauth.config.TestConfiguration;
import com.darglk.blogauth.connector.KeycloakRealm;
import com.darglk.blogauth.repository.AuthorityRepository;
import com.darglk.blogauth.repository.PasswordResetTokenRepository;
import com.darglk.blogauth.repository.UserRepository;
import com.darglk.blogauth.repository.entity.AuthorityEntity;
import com.darglk.blogauth.repository.entity.PasswordResetTokenEntity;
import com.darglk.blogauth.repository.entity.UserEntity;
import com.darglk.blogauth.rest.model.PasswordResetRequest;
import com.darglk.blogauth.rest.model.VerifyAccountActivationTokenRequest;
import com.darglk.blogauth.rest.model.VerifyPasswordResetTokenRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
public class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private KeycloakRealm keycloakRealm;

    private ObjectMapper objectMapper = new ObjectMapper();
    private String userId = "user_id";
    private String token = "reset_token";
    private String tokenId = "reset_token_id";

    @BeforeEach
    public void setup() {
        createUser();
    }

    @AfterEach
    public void teardown() {
        passwordResetTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void generateToken_blankEmail() throws Exception {
        var request = new PasswordResetRequest("");

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/password-reset")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(2)))
                .andExpect(jsonPath("$.errors.[0].field").value("email"))
                .andExpect(jsonPath("$.errors.[*].message").value(Matchers.containsInAnyOrder("must not be blank", "size must be between 4 and 100")));
    }

    @Test
    public void generateToken_userNotEnabled() throws Exception {
        var user = userRepository.findById(userId).get();
        user.setEnabled(false);
        userRepository.save(user);
        var request = new PasswordResetRequest("testing@test.com");

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/password-reset")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        var token = passwordResetTokenRepository.findByUserId(userId);
        assertTrue(token.isEmpty());
    }

    @Test
    public void generateToken_userNotExists() throws Exception {
        var request = new PasswordResetRequest("idont@exist.com");

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/password-reset")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void generateToken() throws Exception {
        var request = new PasswordResetRequest("testing@test.com");

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/password-reset")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var token = passwordResetTokenRepository.findByUserId(userId);
        assertTrue(token.isPresent());
    }

    @Test
    public void verifyToken_tokenDoesNotExist() throws Exception {
        var request = new VerifyPasswordResetTokenRequest("token_id", "asdf1234");

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/password-reset/verify/asdf")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message").value("Not found token with id: asdf"));
    }

    @Test
    public void verifyToken_tokensDoNotMatch() throws Exception {
        createToken();
        var request = new VerifyPasswordResetTokenRequest("token_id", "asdf1234");

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/password-reset/verify/" + tokenId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message").value("Tokens do not match"));
    }

    @Test
    public void verifyToken_tokenIsExpired() throws Exception {
        createToken();
        var request = new VerifyPasswordResetTokenRequest(token, "asdf1234");
        var tokenEntity = passwordResetTokenRepository.findById(tokenId).get();
        tokenEntity.setCreatedAt(Instant.now().minus(25, HOURS));
        passwordResetTokenRepository.save(tokenEntity);

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/password-reset/verify/" + tokenId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message").value("Token is expired"));
        assertTrue(passwordResetTokenRepository.findById(tokenId).isEmpty());
    }

    @Test
    public void verifyToken_userIsDisabled() throws Exception {
        createToken();
        var request = new VerifyPasswordResetTokenRequest(token, "asdf1234");
        var user = userRepository.findById(userId).get();
        user.setEnabled(false);
        userRepository.save(user);

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/password-reset/verify/" + tokenId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message").value("User is not enabled"));
        assertTrue(passwordResetTokenRepository.findById(tokenId).isEmpty());
    }

    @Test
    public void verifyToken() throws Exception {
        createToken();
        var request = new VerifyPasswordResetTokenRequest(token, "asdf1234");
        var oldHash = userRepository.findById(userId).get().getPasswordHash();
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/password-reset/verify/" + tokenId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertTrue(passwordResetTokenRepository.findById(tokenId).isEmpty());
        verify(keycloakRealm, times(1)).updatePassword(userId, "asdf1234");
        var user = userRepository.findById(userId).get();
        assertNotEquals(oldHash, user.getPasswordHash());
    }

    private void createToken() {
        var passwordResetToken = new PasswordResetTokenEntity();
        passwordResetToken.setToken(token);
        passwordResetToken.setId(tokenId);
        passwordResetToken.setCreatedAt(Instant.now().plus(12, HOURS));
        passwordResetToken.setUserId(userId);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    private void createUser() {
        var authority = new AuthorityEntity();
        authority.setId("ROLE_USER");
        authority.setName("ROLE_USER");
        var user = new UserEntity();
        user.setEmail("testing@test.com");
        user.setPasswordHash("asdf");
        user.setId(userId);
        user.setEnabled(true);
        user.setAuthorities(List.of(authority));
        userRepository.save(user);
    }
}
