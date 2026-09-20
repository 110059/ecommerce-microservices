package com.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.dto.PaymentEvent;
import com.order.entity.Order;
import com.order.repository.OrderRepository;
import com.order.service.ProductClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentEventListener {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public PaymentEventListener(
            ObjectMapper objectMapper,
            OrderRepository orderRepository,
            ProductClient productClient) {

        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }
    @KafkaListener(
            topics = "payment-completed",
            groupId = "order-payment-group"
    )
    @Transactional
    public void handlePaymentCompleted(String message) {

        try {

            PaymentEvent event =
                    objectMapper.readValue(
                            message,
                            PaymentEvent.class);

            Order order =
                    orderRepository.findById(
                                    event.getOrderId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Order not found: "
                                                    + event.getOrderId()));

            // Idempotency:
            // Don't process an already completed order again.
            if ("PAYMENT_COMPLETED".equals(order.getStatus())) {
                return;
            }

            order.setStatus("PAYMENT_COMPLETED");

            orderRepository.save(order);

            System.out.println(
                    "Payment completed. orderId="
                            + event.getOrderId());

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to process payment-completed event",
                    e);
        }
    }

    @KafkaListener(
            topics = "payment-failed",
            groupId = "order-payment-group"
    )
    @Transactional
    public void handlePaymentFailed(String message) {

        try {

            PaymentEvent event =
                    objectMapper.readValue(
                            message,
                            PaymentEvent.class);

            Order order =
                    orderRepository.findById(
                                    event.getOrderId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Order not found: "
                                                    + event.getOrderId()));

            // Idempotency
            if ("PAYMENT_FAILED".equals(order.getStatus())) {
                return;
            }

            // Restore reserved stock
            productClient.restoreStock(
                    order.getProductId(),
                    order.getQuantity()
            );

            // Update order status
            order.setStatus("PAYMENT_FAILED");

            orderRepository.save(order);

            System.out.println(
                    "Payment failed. Stock restored. orderId="
                            + event.getOrderId());

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to process payment-failed event",
                    e);
        }
    }
}