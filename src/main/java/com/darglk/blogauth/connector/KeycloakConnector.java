package com.darglk.blogauth.connector;

import com.darglk.blogauth.rest.model.KeycloakLoginResponse;
import com.darglk.blogcommons.model.LoginRequest;
import lombok.extern.slf4j.Slf4j;
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
            var uri = new URI("http://keycloak-srv:8080/realms/blog/protocol/openid-connect/token");
            return restTemplate.postForEntity(uri, httpEntity, KeycloakLoginResponse.class).getBody();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Coś jebło");
        }
        return null;
    }
}
