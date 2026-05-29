package ch.hatbe.jbof.auth;

import ch.hatbe.jbof.user.UserService;
import ch.hatbe.jbof.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;

    @GetMapping("/me")
    private User getMe(@AuthenticationPrincipal Jwt jwt) {
       return this.userService.findByKeycloakUserId(UUID.fromString(jwt.getSubject()));
    }

    @GetMapping("/me/authorities")
    public Collection<? extends GrantedAuthority> authorities(Authentication authentication) {
        return authentication.getAuthorities();
    }
}
