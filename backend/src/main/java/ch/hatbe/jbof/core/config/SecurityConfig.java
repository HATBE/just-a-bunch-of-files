package ch.hatbe.jbof.core.config;

import ch.hatbe.jbof.auth.KeycloakJwtGrantedAuthoritiesConverter;
import ch.hatbe.jbof.auth.UserAccessFilter;
import ch.hatbe.jbof.auth.UserAccessVerifier;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AppProperties props;
    private final UserAccessVerifier userAccessVerifier;

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            UserAccessFilter userAccessFilter,
            AccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(r -> r
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(props.getSecurity().publicPaths().toArray(String[]::new)).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(a -> a
                        .jwt(t -> t.jwtAuthenticationConverter(jwtAuthenticationConverter))
                )
                .exceptionHandling(e -> e
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterAfter(
                        userAccessFilter,
                        BearerTokenAuthenticationFilter.class
                )
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName("preferred_username");
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtGrantedAuthoritiesConverter());
        return converter;
    }

    @Bean
    UserAccessFilter userAccessFilter(AccessDeniedHandler accessDeniedHandler) {
        return new UserAccessFilter(userAccessVerifier, accessDeniedHandler);
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            if (!response.isCommitted()) {
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        accessDeniedException.getMessage()
                );
            }
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(false);

        config.setAllowedOriginPatterns(allowedOrigins());

        config.setAllowedHeaders(Arrays.asList(
                HttpHeaders.ORIGIN,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                HttpHeaders.AUTHORIZATION
        ));

        config.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        config.setExposedHeaders(List.of("Location"));

        source.registerCorsConfiguration("/api/**", config);

        return source;
    }

    private List<String> allowedOrigins() {
        return Stream.concat(
                Stream.of(
                        "http://localhost:*",
                        "http://127.0.0.1:*"
                ),
                Optional.ofNullable(this.props.getCors())
                        .map(AppProperties.Cors::allowedOrigins)
                        .orElse(List.of())
                        .stream()
        ).toList();
    }
}