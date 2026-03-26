package ch.hatbe.jbof.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(S3Config.S3Props.class)
public class S3Config {
    @Bean
    public S3Client s3Client(S3Props props) {
        var creds = AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey());

        var s3cfg = S3Configuration.builder()
                .pathStyleAccessEnabled(props.isPathStyle())
                .build();

        return S3Client.builder()
                .endpointOverride(URI.create(props.getEndpoint()))
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .serviceConfiguration(s3cfg)
                .build();
    }

    @Data
    @ConfigurationProperties(prefix = "storage.s3")
    public static class S3Props {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String region = "us-east-1";
        private boolean pathStyle = true;
    }
}