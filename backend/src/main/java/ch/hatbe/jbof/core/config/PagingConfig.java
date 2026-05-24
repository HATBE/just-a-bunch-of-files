package ch.hatbe.jbof.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
@RequiredArgsConstructor
public class PagingConfig {
    private final AppProperties props;

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableHandlerMethodArgumentResolverCustomizer() {
        return r -> {
            r.setFallbackPageable(PageRequest.of(0, this.props.getPagination().defaultPageSize()));
            r.setMaxPageSize(this.props.getPagination().maxPageSize());
        };
    }
}
