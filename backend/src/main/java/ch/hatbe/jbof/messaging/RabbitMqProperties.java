package ch.hatbe.jbof.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq")
public record RabbitMqProperties(
        String defaultExchange,
        String defaultRoutingKey,
        long receiveTimeout,
        String mediaProcessingQueue
) {
}
