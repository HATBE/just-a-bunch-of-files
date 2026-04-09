package ch.hatbe.jbof.core.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class PermissionService {
    public boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(permission));
    }

    public void requirePermission(String permission) {
        if (!hasPermission(permission)) {
            throw new AccessDeniedException("missing permission: " + permission);
        }
    }
}
