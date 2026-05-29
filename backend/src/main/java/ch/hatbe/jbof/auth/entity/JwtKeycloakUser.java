package ch.hatbe.jbof.auth.entity;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public record JwtKeycloakUser(
        UUID sub,
        String email,
        String preferredUsername,
        String givenName,
        String familyName
) {
    public static JwtKeycloakUser fromJwt(Jwt jwt) {
        String subject = jwt.getSubject();

        if (subject == null || subject.isBlank()) {
            throw new BadCredentialsException("Missing JWT subject");
        }

        UUID sub;
        try {
            sub = UUID.fromString(subject);
        } catch (IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid JWT subject", ex);
        }

        String email = jwt.getClaimAsString("email");
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        String givenName = jwt.getClaimAsString("given_name");
        String familyName = jwt.getClaimAsString("family_name");

        return new JwtKeycloakUser(
                sub,
                email,
                preferredUsername,
                givenName,
                familyName
        );
    }
}