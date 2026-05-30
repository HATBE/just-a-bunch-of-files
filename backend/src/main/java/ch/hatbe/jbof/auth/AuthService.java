package ch.hatbe.jbof.auth;

import ch.hatbe.jbof.auth.entity.request.CreateUserRequest;
import ch.hatbe.jbof.core.exception.ConflictException;
import ch.hatbe.jbof.user.UserService;
import ch.hatbe.jbof.user.entity.dto.UserListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final KeycloakAdminService keycloakAdminService;

    public UserListDto register(CreateUserRequest req) throws ConflictException {
        if (userService.existsByUsername(req.username())) {
            throw new ConflictException("Username already exists");
        }

        if (userService.existsByEmail(req.email())) {
            throw new ConflictException("Email already exists");
        }

        UUID keycloakId = keycloakAdminService.create(req);

        try {
            return userService.create(req, keycloakId);
        } catch (RuntimeException ex) {
            keycloakAdminService.delete(keycloakId);
            throw ex;
        }
    }
}
