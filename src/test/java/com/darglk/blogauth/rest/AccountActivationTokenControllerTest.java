package com.darglk.blogauth.rest;

import com.darglk.blogauth.BlogAuthApplication;
import com.darglk.blogauth.config.TestConfiguration;
import com.darglk.blogauth.repository.AccountActivationTokenRepository;
import com.darglk.blogauth.repository.AuthorityRepository;
import com.darglk.blogauth.repository.UserAuthorityRepository;
import com.darglk.blogauth.repository.UserRepository;
import com.darglk.blogauth.repository.entity.AccountActivationTokenEntity;
import com.darglk.blogauth.repository.entity.AuthorityEntity;
import com.darglk.blogauth.repository.entity.UserEntity;
import com.darglk.blogauth.rest.model.VerifyAccountActivationTokenRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
public class AccountActivationTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private UserAuthorityRepository userAuthorityRepository;
    @Autowired
    private AccountActivationTokenRepository accountActivationTokenRepository;

    private ObjectMapper objectMapper = new ObjectMapper();
    private String userId = "user_id";
    private String token = "activation_token";
    private String tokenId = "activation_token_id";

    @BeforeEach
    public void setup() {
        createUser();
        createToken();
    }

    @AfterEach
    public void teardown() {
        accountActivationTokenRepository.deleteAll();
        userAuthorityRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void verifyToken_notFound() throws Exception {
        var request = new VerifyAccountActivationTokenRequest("tokennotexist");

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/token/asdf")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message").value("Not found token with id: asdf"));
    }

    @Test
    void verifyToken_tokensDoNotMatch() throws Exception {
        var request = new VerifyAccountActivationTokenRequest("tokennotexist");

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/token/" + tokenId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message").value("Tokens do not match"));
    }

    @Test
    void verifyToken_tokenIsExpired() throws Exception {
        var accountActivationToken = accountActivationTokenRepository.findById(tokenId).get();
        accountActivationToken.setCreatedAt(Instant.now().minus(25L, HOURS));
        accountActivationTokenRepository.save(accountActivationToken);

        var request = new VerifyAccountActivationTokenRequest(token);

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/token/" + tokenId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message").value("Token is expired"));

        assertTrue(accountActivationTokenRepository.findById(tokenId).isEmpty());
    }

    @Test
    void verifyToken() throws Exception {
        var request = new VerifyAccountActivationTokenRequest(token);

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/users/token/" + tokenId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        var user = userRepository.findById(userId);

        assertTrue(user.get().getEnabled());
    }

    private void createToken() {
        var accountActivationToken = new AccountActivationTokenEntity();
        accountActivationToken.setToken(token);
        accountActivationToken.setId(tokenId);
        accountActivationToken.setCreatedAt(Instant.now().plus(12, HOURS));
        accountActivationToken.setUserId(userId);
        accountActivationTokenRepository.save(accountActivationToken);
    }

    private void createUser() {
        var authority = new AuthorityEntity();
        authority.setId("ROLE_USER");
        authority.setName("ROLE_USER");
        var user = new UserEntity();
        user.setEmail("testing@test.com");
        user.setId(userId);
        user.setEnabled(false);
        user.setAuthorities(List.of(authority));
        userRepository.save(user);
    }
}
