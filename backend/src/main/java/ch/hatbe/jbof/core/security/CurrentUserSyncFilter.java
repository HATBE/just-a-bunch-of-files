package ch.hatbe.jbof.core.security;

import ch.hatbe.jbof.user.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CurrentUserSyncFilter extends OncePerRequestFilter {
    private final AuthenticatedUserService authenticatedUserService;
    private final CurrentUserContext currentUserContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            User user = this.authenticatedUserService.synchronize(jwtAuthenticationToken.getToken());
            this.currentUserContext.setCurrentUser(user);
        }

        filterChain.doFilter(request, response);
    }
}
