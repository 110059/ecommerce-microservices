package com.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.dto.OrderResponse;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {

    private static final String ORDER_CREATED_TOPIC = "order-created";
    private static final String ORDER_CANCELLED_TOPIC = "order-cancelled";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendOrderEvent(OrderResponse order) {

        sendEvent(ORDER_CREATED_TOPIC, order);
    }

    public void sendOrderCancelledEvent(OrderResponse order) {

        sendEvent(ORDER_CANCELLED_TOPIC, order);
    }

    private void sendEvent(String topic, OrderResponse order) {

        try {

            String message = objectMapper.writeValueAsString(order);

            kafkaTemplate.send(topic, message)
                    .whenComplete((result, exception) -> {

                        if (exception != null) {

                            System.err.println(
                                    "Failed to send Kafka event: "
                                            + exception.getMessage()
                            );

                        } else {

                            System.out.println(
                                    "Kafka event sent successfully. topic="
                                            + topic
                                            + ", message="
                                            + message
                            );
                        }
                    });

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to convert order to JSON", e);
        }
    }

}