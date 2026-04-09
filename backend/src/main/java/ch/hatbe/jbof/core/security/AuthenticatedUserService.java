package ch.hatbe.jbof.core.security;

import ch.hatbe.jbof.user.UserRepository;
import ch.hatbe.jbof.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticatedUserService {
    private final UserRepository userRepository;
    private final CurrentUserContext currentUserContext;

    @Transactional
    public User synchronize(Jwt jwt) {
        String keycloakUserId = this.requiredClaim(jwt, "sub");
        String username = this.resolveUsername(jwt);

        return this.userRepository.findByKeycloakUserId(keycloakUserId)
                .map(user -> this.updateUsernameIfNeeded(user, username))
                .orElseGet(() -> this.createUser(keycloakUserId, username));
    }

    public User getCurrentUser() {
        return this.currentUserContext.getRequiredUser();
    }

    private String requiredClaim(Jwt jwt, String claimName) {
        String value = jwt.getClaimAsString(claimName);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("missing token claim: " + claimName);
        }

        return value;
    }

    private String resolveUsername(Jwt jwt) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }

        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return email;
        }

        return this.requiredClaim(jwt, "sub");
    }

    private User updateUsernameIfNeeded(User user, String username) {
        if (!username.equals(user.getUsername())) {
            user.setUsername(username);
        }

        return user;
    }

    private User createUser(String keycloakUserId, String username) {
        User user = new User();
        user.setKeycloakUserId(keycloakUserId);
        user.setUsername(username);
        user.setCreatedAt(OffsetDateTime.now());
        return this.userRepository.save(user);
    }
}
