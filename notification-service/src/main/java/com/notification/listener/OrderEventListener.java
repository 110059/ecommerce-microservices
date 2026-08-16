package com.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.dto.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventListener {

    private final ObjectMapper objectMapper;

    public OrderEventListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = {"order-created", "order-cancelled"},
            groupId = "notification-group"
    )
    public void consume(String message) throws Exception {

        OrderEvent event =
                objectMapper.readValue(message, OrderEvent.class);

        throw new RuntimeException("TEST KAFKA FAILURE");
        /*
        System.out.println("=================================");
        System.out.println("       ORDER NOTIFICATION");
        System.out.println("=================================");
        System.out.println("Order ID    : " + event.getOrderId());
        System.out.println("User ID     : " + event.getUserId());
        System.out.println("Product ID  : " + event.getProductId());
        System.out.println("Quantity    : " + event.getQuantity());
        System.out.println("Total Price : " + event.getTotalPrice());
        System.out.println("Status      : " + event.getStatus());
        System.out.println("=================================");
        */

    }
}