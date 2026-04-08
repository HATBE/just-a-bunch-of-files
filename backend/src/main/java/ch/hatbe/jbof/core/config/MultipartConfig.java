package ch.hatbe.jbof.core.config;

import jakarta.servlet.MultipartConfigElement;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
@EnableConfigurationProperties(MultipartConfig.MultipartProps.class)
public class MultipartConfig {
    @Bean
    public MultipartConfigElement multipartConfigElement(MultipartProps props) {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofGigabytes(props.maxFileSize));
        factory.setMaxRequestSize(DataSize.ofGigabytes(props.maxRequestSize));
        return factory.createMultipartConfig();
    }

    @Data
    @ConfigurationProperties(prefix = "multipart")
    public static class MultipartProps {
        private int maxFileSize = 10;
        private int maxRequestSize = 10;
    }
}
