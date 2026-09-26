package com.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.dto.StockReservedEvent;
import com.order.entity.Order;
import com.order.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StockReservedEventListener {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    public StockReservedEventListener(
            ObjectMapper objectMapper,
            OrderRepository orderRepository) {
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
    }

    @KafkaListener(
            topics = "stock-reserved",
            groupId = "order-stock-group"
    )
    @Transactional
    public void handleStockReserved(String message) {

        try {
            StockReservedEvent event =
                    objectMapper.readValue(
                            message,
                            StockReservedEvent.class);

            Order order =
                    orderRepository.findById(
                                    event.getOrderId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Order not found: "
                                                    + event.getOrderId()));

            if ("STOCK_RESERVED".equals(order.getStatus())
                    || "PAYMENT_COMPLETED".equals(order.getStatus())
                    || "PAYMENT_FAILED".equals(order.getStatus())) {
                return;
            }

            order.setStatus("STOCK_RESERVED");

            orderRepository.save(order);

            System.out.println(
                    "Stock reserved. orderId="
                            + event.getOrderId());

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to process stock-reserved event",
                    e);
        }
    }
}