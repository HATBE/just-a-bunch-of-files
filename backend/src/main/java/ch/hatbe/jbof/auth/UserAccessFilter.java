package ch.hatbe.jbof.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class UserAccessFilter extends OncePerRequestFilter {
    private final UserAccessVerifier userAccessVerifier;
    private final AccessDeniedHandler accessDeniedHandler;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken
                && jwtAuthenticationToken.isAuthenticated()) {
            try {
                userAccessVerifier.requireExistingAppUser(jwtAuthenticationToken.getToken());
            } catch (AccessDeniedException ex) {
                accessDeniedHandler.handle(request, response, new AccessDeniedException("No enough Permission!"));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}