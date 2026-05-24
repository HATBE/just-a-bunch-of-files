package ch.hatbe.jbof;

import ch.hatbe.jbof.core.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class JbofApplication {

	static void main(String[] args) {
		SpringApplication.run(JbofApplication.class, args);
	}

}
