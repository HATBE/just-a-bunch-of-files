package ch.hatbe.jbof.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Cors cors;
    private Security security;
    private Auth auth;
    private Pagination pagination;

    public record Cors (
            List<String> allowedOrigins
    ) {}

    public record Security (
            List<String> publicPaths
    ) {}

    public record Auth (
            String baseUrl,
            String realm,
            String adminClientId,
            String adminClientSecret
    ) {}

    public record Pagination (
            int maxPageSize,
            int defaultPageSize
    ) {}
}
