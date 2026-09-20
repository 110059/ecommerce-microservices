package com.product.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.product.dto.StockReservedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class StockEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public StockEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendStockReserved(StockReservedEvent event) {

        try {

            String message =
                    objectMapper.writeValueAsString(event);

            kafkaTemplate.send(
                    "stock-reserved",
                    event.getOrderId().toString(),
                    message
            );

            System.out.println(
                    "Stock reserved event sent | orderId: "
                            + event.getOrderId());

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to serialize stock reserved event",
                    e);
        }
    }
}