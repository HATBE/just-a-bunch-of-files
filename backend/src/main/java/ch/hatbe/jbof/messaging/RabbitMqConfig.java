package ch.hatbe.jbof.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RabbitMqProperties.class)
public class RabbitMqConfig {
    @Bean
    public Queue mediaProcessingQueue(RabbitMqProperties properties) {
        return new Queue(properties.mediaProcessingQueue(), true);
    }

    @Bean
    public JacksonJsonMessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter messageConverter,
            RabbitMqProperties properties
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setReceiveTimeout(properties.receiveTimeout());

        if (properties.defaultExchange() != null && !properties.defaultExchange().isBlank()) {
            rabbitTemplate.setExchange(properties.defaultExchange());
        }

        if (properties.defaultRoutingKey() != null && !properties.defaultRoutingKey().isBlank()) {
            rabbitTemplate.setRoutingKey(properties.defaultRoutingKey());
        }

        return rabbitTemplate;
    }
}
