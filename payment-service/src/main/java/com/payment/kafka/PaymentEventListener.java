package com.payment.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.dto.PaymentEvent;
import com.payment.dto.StockReservedEvent;
import com.payment.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentEventListener(
            ObjectMapper objectMapper,
            PaymentService paymentService,
            PaymentEventProducer paymentEventProducer) {
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
        this.paymentEventProducer = paymentEventProducer;
    }

    @KafkaListener(
            topics = "stock-reserved",
            groupId = "payment-group"
    )
    public void consume(String message) {

        try {
            StockReservedEvent event =
                    objectMapper.readValue(message, StockReservedEvent.class);

            System.out.println(
                    "Stock reserved event received | orderId: "
                            + event.getOrderId());

            boolean paymentSuccessful =
                    paymentService.processPayment(event);

            PaymentEvent paymentEvent = new PaymentEvent(
                    event.getOrderId(),
                    event.getUserId(),
                    event.getProductId(),
                    event.getQuantity(),
                    event.getTotalPrice(),
                    paymentSuccessful ? "COMPLETED" : "FAILED"
            );

            if (paymentSuccessful) {
                paymentEventProducer.sendPaymentCompleted(paymentEvent);
            } else {
                paymentEventProducer.sendPaymentFailed(paymentEvent);
            }

        } catch (Exception e) {
            System.err.println(
                    "Failed to process stock-reserved event: "
                            + e.getMessage());

            throw new RuntimeException(
                    "Payment processing failed", e);
        }
    }
}