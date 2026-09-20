package com.payment.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.dto.PaymentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendPaymentCompleted(PaymentEvent event) {
        send("payment-completed", event);
    }

    public void sendPaymentFailed(PaymentEvent event) {
        send("payment-failed", event);
    }

    private void send(String topic, PaymentEvent event) {

        try {
            String message = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(topic, event.getOrderId().toString(), message);

            System.out.println(
                    "Payment event sent to topic: " + topic +
                            " | orderId: " + event.getOrderId());

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payment event", e);
        }
    }
}