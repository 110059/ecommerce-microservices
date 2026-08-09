package com.notification.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @KafkaListener(topics = "order-created", groupId = "group1")
    public void consume(String msg) {

        System.out.println("Notification Received: " + msg);
    }
}