package com.darglk.blogauth.connector;

import com.darglk.blogauth.rest.model.KeycloakLoginResponse;
import com.darglk.blogcommons.exception.BadRequestException;
import com.darglk.blogcommons.model.LoginRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Component
@Slf4j
@Profile("!test")
public class KeycloakConnector {

    @Value("${keycloak.server.url}")
    private String keycloakServerUrl;
    @Value("${keycloak.server.port}")
    private String keycloakServerPort;
    @Value("${keycloak.api.realm}")
    private String keycloakApiRealm;

    private final RestTemplate restTemplate;

    public KeycloakConnector() {
        restTemplate = new RestTemplateBuilder()
                .build();
    }

    public KeycloakLoginResponse signIn(LoginRequest loginRequest) {
        var httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_FORM_URLENCODED));
        var map = new LinkedMultiValueMap<String, String>();
        map.put("client_id", List.of("account"));
        map.put("grant_type", List.of("password"));
        map.put("scope", List.of("openid"));
        map.put("username", List.of(loginRequest.getEmail()));
        map.put("password", List.of(loginRequest.getPassword()));
        var httpEntity = new HttpEntity<MultiValueMap<String, String>>(map, httpHeaders);
        try {
            var url = String.format("http://%s:%s/realms/%s/protocol/openid-connect/token",
                    keycloakServerUrl, keycloakServerPort, keycloakApiRealm);
            var uri = new URI(url);
            return restTemplate.postForEntity(uri, httpEntity, KeycloakLoginResponse.class).getBody();
        } catch (Exception e) {
            log.error("Keycloak login endpoint returned errors: {}", e.getMessage());
            throw new BadRequestException("Invalid login credentials");
        }
    }

    public KeycloakLoginResponse refreshToken(String refresh_token) {
        var httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_FORM_URLENCODED));
        var map = new LinkedMultiValueMap<String, String>();
        map.put("client_id", List.of("account"));
        map.put("grant_type", List.of("refresh_token"));
        map.put("refresh_token", List.of(refresh_token));

        var httpEntity = new HttpEntity<MultiValueMap<String, String>>(map, httpHeaders);
        try {
            var url = String.format("http://%s:%s/realms/%s/protocol/openid-connect/token",
                    keycloakServerUrl, keycloakServerPort, keycloakApiRealm);
            var uri = new URI(url);
            return restTemplate.postForEntity(uri, httpEntity, KeycloakLoginResponse.class).getBody();
        } catch (Exception e) {
            log.error("Keycloak refresh token endpoint returned errors: {}", e.getMessage());
            throw new BadRequestException("Invalid login credentials");
        }
    }
}
