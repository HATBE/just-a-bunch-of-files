package ch.hatbe.jbof.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
@EnableConfigurationProperties(PagingConfig.PaginationProperties.class)
public class PagingConfig {
    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer(PaginationProperties props) {
        return resolver -> {
            resolver.setFallbackPageable(PageRequest.of(0, props.defaultPageSize));
            resolver.setMaxPageSize(props.maxPageSize);
        };
    }

    @Data
    @ConfigurationProperties(prefix = "pagination")
    public static class PaginationProperties {
        private int maxPageSize = 200;
        private int defaultPageSize = 20;
    }
}