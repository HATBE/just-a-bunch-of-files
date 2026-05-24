package ch.hatbe.jbof.core.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class MultipartConfig {
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory f = new MultipartConfigFactory();

        f.setMaxFileSize(DataSize.ofGigabytes(1));
        f.setMaxRequestSize(DataSize.ofGigabytes(1));

        return f.createMultipartConfig();
    }
}
