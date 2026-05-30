package ch.hatbe.jbof.auth;


import ch.hatbe.jbof.user.UserRepository;
import ch.hatbe.jbof.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@RequestScope
@Slf4j
public class CurrentUser {
    private final UserRepository userRepository;

    private User cachedUser;

    private UUID getKeycloakUserId() {
        return UUID.fromString(jwt().getSubject());
    }

    private Jwt jwt() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication instanceof JwtAuthenticationToken token && token.isAuthenticated()) {
            return token.getToken();
        }

        throw new AuthenticationCredentialsNotFoundException("No authenticated JWT found");
    }

    @Transactional(readOnly = true)
    public User get() {
        if (cachedUser == null) {
            cachedUser = userRepository.findByKeycloakUserId(this.getKeycloakUserId())
                    .orElseThrow(() -> new AccessDeniedException("User is not registered in this application"));
        }

        return cachedUser;
    }
}
