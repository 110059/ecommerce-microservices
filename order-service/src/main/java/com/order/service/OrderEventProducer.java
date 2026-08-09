package com.order.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {

        @Autowired
        private KafkaTemplate<String, String> kafkaTemplate;

        public String sendOrderEvent(String event) {

            kafkaTemplate.send("order-created", event);

            System.out.println("Event Sent To Kafka");
            return event;
        }
    }

