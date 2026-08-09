package com.product.repository;

import com.product.model.ProcessedRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedRequestRepository
        extends JpaRepository<ProcessedRequest, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);
}