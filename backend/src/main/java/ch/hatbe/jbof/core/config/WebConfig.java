package ch.hatbe.jbof.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final AppProperties props;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/api/**")
                .allowCredentials(false) // we use jwt coming from keycloak
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Accept")
                .exposedHeaders("Location")
                .allowedOriginPatterns(this.allowedOriginPatterns().toArray(String[]::new));
    }

    private List<String> allowedOriginPatterns() {
        return Stream.concat(
                Stream.of(
                        "http://localhost:*",
                        "http://127.0.0.1:*"
                ),
                Optional.ofNullable(props.getCors())
                        .map(AppProperties.Cors::allowedOrigins)
                        .orElse(List.of())
                        .stream()
        ).toList();
    }
}