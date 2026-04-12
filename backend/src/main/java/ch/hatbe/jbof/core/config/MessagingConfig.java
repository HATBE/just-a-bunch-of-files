package ch.hatbe.jbof.core.config;

import lombok.Data;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MessagingConfig.MessagingProps.class)
public class MessagingConfig {
    @Bean
    public Queue mediaProcessingQueue(MessagingProps props) {
        return new Queue(props.mediaProcessingQueue, true);
    }

    @Bean
    public JacksonJsonMessageConverter queueMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter messageConverter,
            MessagingProps props
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setReceiveTimeout(props.receiveTimeout);

        if (props.defaultExchange != null && !props.defaultExchange.isBlank()) {
            rabbitTemplate.setExchange(props.defaultExchange);
        }

        if (props.defaultRoutingKey != null && !props.defaultRoutingKey.isBlank()) {
            rabbitTemplate.setRoutingKey(props.defaultRoutingKey);
        }

        return rabbitTemplate;
    }

    @Data
    @ConfigurationProperties(prefix = "messaging")
    public static class MessagingProps {
        public String defaultExchange;
        public String defaultRoutingKey;
        public long receiveTimeout;
        public String mediaProcessingQueue;
    }
}
