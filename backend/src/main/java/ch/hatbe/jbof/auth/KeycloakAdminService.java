package ch.hatbe.jbof.auth;

import ch.hatbe.jbof.auth.entity.request.CreateUserRequest;
import ch.hatbe.jbof.core.config.AppProperties;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class KeycloakAdminService {
    private final Keycloak keycloak;
    private final AppProperties props;

    public UUID create(CreateUserRequest req) {
        UserRepresentation user = new UserRepresentation();

        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setFirstName(req.firstname().isBlank() ? "NO_DATA" : req.firstname());
        user.setLastName(req.lastname().isBlank()  ? "NO_DATA" : req.lastname());
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setGroups(List.of("/user")); // default group of each User
        user.setRequiredActions(List.of());

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setTemporary(false);
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(req.password());

        user.setCredentials(List.of(cred));

        try (Response res = this.usersResource().create(user)) {
            if (res.getStatus() != Response.Status.CREATED.getStatusCode()) {
                String body = res.readEntity(String.class);
                throw new IllegalStateException("Could not create Keycloak user. Status: " + res.getStatus() + ", body: " + body);
            }
            return UUID.fromString(CreatedResponseUtil.getCreatedId(res));
        }
    }

    public void delete(UUID keycloakUserId) {
        this.usersResource()
                .get(keycloakUserId.toString())
                .remove();
    }

    private UsersResource usersResource() {
        return this.keycloak
                .realm(props.getAuth().realm())
                .users();
    }
}
