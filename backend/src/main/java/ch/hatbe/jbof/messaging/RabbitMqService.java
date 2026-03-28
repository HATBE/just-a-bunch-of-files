package ch.hatbe.jbof.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMqService {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties properties;

    public void sendToQueue(String queueName, Object payload) {
        rabbitTemplate.convertAndSend(queueName, payload);
    }

    public void publish(String exchange, String routingKey, Object payload) {
        rabbitTemplate.convertAndSend(exchange, routingKey, payload);
    }

    public void publish(Object payload) {
        rabbitTemplate.convertAndSend(
                properties.defaultExchange(),
                properties.defaultRoutingKey(),
                payload
        );
    }

    public <T> T receive(String queueName, Class<T> responseType) {
        Object payload = rabbitTemplate.receiveAndConvert(queueName);
        if (payload == null) {
            return null;
        }

        return responseType.cast(payload);
    }

    public <T> T sendAndReceive(String exchange, String routingKey, Object payload, Class<T> responseType) {
        Object response = rabbitTemplate.convertSendAndReceive(exchange, routingKey, payload);
        if (response == null) {
            return null;
        }

        return responseType.cast(response);
    }
}
