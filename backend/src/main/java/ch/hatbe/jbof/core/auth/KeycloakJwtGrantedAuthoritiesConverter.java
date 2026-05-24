package ch.hatbe.jbof.core.auth;

import java.util.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public final class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Object realmAccessObject = jwt.getClaims().get("realm_access");
        if (realmAccessObject instanceof Map<?, ?> realmAccess && realmAccess.get("roles") instanceof Collection<?> roles) {
            roles.stream().filter(String.class::isInstance).map(String.class::cast)
                    .filter(role -> !role.startsWith("default-roles-"))
                    .map(SimpleGrantedAuthority::new).forEach(authorities::add);
        }
        /* Object groupsObject = jwt.getClaims().get("groups");
        if (groupsObject instanceof Collection<?> groups) {
            groups.stream().filter(String.class::isInstance).map(String.class::cast)
                    .map(group -> "GROUP_" + group.replaceFirst("^/", ""))
                    .map(SimpleGrantedAuthority::new).forEach(authorities::add);
        }*/
        return authorities;
    }
}
