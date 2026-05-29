package ch.hatbe.jbof.auth;

import ch.hatbe.jbof.auth.entity.JwtKeycloakUser;
import ch.hatbe.jbof.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccessVerifier {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public void requireExistingAppUser(Jwt token) {
        JwtKeycloakUser keycloakUser = JwtKeycloakUser.fromJwt(token);

        if (!userRepository.existsByKeycloakUserId(keycloakUser.sub())) {
            throw new AccessDeniedException("User is not registered in this application");
        }
        // TODO: check if the user is deleted or blocked
    }
}