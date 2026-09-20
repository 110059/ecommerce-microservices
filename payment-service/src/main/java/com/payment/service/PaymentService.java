package com.payment.service;

import com.payment.dto.StockReservedEvent;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public boolean processPayment(StockReservedEvent event) {

        // Simulate successful payment for now.
        // Failure logic will be added later for Saga compensation testing.
        return true;
    }
}