package ch.hatbe.jbof.core.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private String clientId;
    private String principalClaim = "preferred_username";
}
