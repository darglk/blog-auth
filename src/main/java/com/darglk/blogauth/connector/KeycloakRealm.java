package com.darglk.blogauth.connector;

import com.darglk.blogauth.rest.model.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class KeycloakRealm {
    @Value("${keycloak.admin.realm}")
    private String realmName;
    private final RealmResource realm;

    public String createUser(SignupRequest signupRequest) {
        var userRepresentation = new UserRepresentation();

        var credentialsRepresentation = new CredentialRepresentation();
        credentialsRepresentation.setTemporary(false);
        credentialsRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialsRepresentation.setValue(signupRequest.getPassword());
        userRepresentation.setEmail(signupRequest.getEmail());
        userRepresentation.setEmailVerified(true);
        userRepresentation.setUsername(signupRequest.getEmail());
        userRepresentation.setEnabled(true);
        userRepresentation.setCredentials(List.of(credentialsRepresentation));
        var response = realm.users()
                .create(userRepresentation);
        return response.getHeaderString("Location")
                .split("(?<=\\/" + realmName + "\\/users\\/)")[1];
    }
}
