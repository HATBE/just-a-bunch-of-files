package ch.hatbe.jbof.messaging;

import ch.hatbe.jbof.core.config.MessagingConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessagingService {
    private final RabbitTemplate rabbitTemplate;
    private final MessagingConfig.MessagingProps props;

    public void sendToQueue(Object payload) {
        this.sendToQueue(props.mediaProcessingQueue, payload);
    }

    public void sendToQueue(String queueName, Object payload) {
        rabbitTemplate.convertAndSend(queueName, payload);
    }

    public void publish(String exchange, String routingKey, Object payload) {
        rabbitTemplate.convertAndSend(exchange, routingKey, payload);
    }

    public void publish(Object payload) {
        rabbitTemplate.convertAndSend(
                props.defaultExchange,
                props.defaultRoutingKey,
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
