package com.product.kafka;

import com.product.entity.OutboxEvent;
import com.product.repository.OutboxEventRepository;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class ProductOutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 30000)
    public void publishEvents() {

        List<OutboxEvent> events =
                outboxEventRepository
                        .findByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEvent event : events) {

            try {

                kafkaTemplate.send(
                        "stock-reserved",
                        event.getAggregateId().toString(),
                        event.getPayload()
                ).get();

                event.setStatus("SENT");

                outboxEventRepository.save(event);

                System.out.println(
                        "Product outbox event published | eventId: "
                                + event.getId()
                                + " | orderId: "
                                + event.getAggregateId()
                );

            } catch (Exception e) {

                System.err.println(
                        "Failed to publish product outbox event | eventId: "
                                + event.getId()
                                + " | error: "
                                + e.getMessage()
                );
            }
        }
    }
}