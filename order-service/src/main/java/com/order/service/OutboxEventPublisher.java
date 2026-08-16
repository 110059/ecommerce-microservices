package com.order.service;

import com.order.entity.OutboxEvent;
import com.order.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboxEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxEventPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxEventPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate) {

        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 20000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository
                        .findByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEvent event : events) {

            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEvent event) {

        String topic = getTopic(event.getEventType());

        try {

            kafkaTemplate.send(
                    topic,
                    event.getAggregateId().toString(),
                    event.getPayload()
            ).get();

            event.setStatus("PROCESSED");
            event.setProcessedAt(
                    java.time.LocalDateTime.now());

            outboxEventRepository.save(event);

            log.info(
                    "Outbox event published successfully. eventId={}, eventType={}, topic={}",
                    event.getId(),
                    event.getEventType(),
                    topic
            );

        } catch (Exception e) {

            log.error(
                    "Failed to publish outbox event. eventId={}, eventType={}",
                    event.getId(),
                    event.getEventType(),
                    e
            );

            // Keep status as PENDING.
            // It will be retried during the next scheduled execution.
        }
    }

    private String getTopic(String eventType) {

        return switch (eventType) {

            case "ORDER_CREATED" ->
                    "order-created";

            case "ORDER_CANCELLED" ->
                    "order-cancelled";

            default ->
                    throw new IllegalArgumentException(
                            "Unknown event type: " + eventType);
        };
    }
}