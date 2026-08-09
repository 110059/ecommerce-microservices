package com.product.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "processed_requests",
        uniqueConstraints = @UniqueConstraint(
                columnNames = "idempotency_key"
        )
)
public class ProcessedRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    public ProcessedRequest() {
    }

    public Long getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}