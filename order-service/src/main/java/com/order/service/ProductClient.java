package com.order.service;

import com.order.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "PRODUCT-SERVICE")
public interface ProductClient {

    @GetMapping("/products/{id}")
    ProductResponse getProduct(@PathVariable("id") Long id);

    @PutMapping("/products/{id}/reduce-stock")
    String reduceStock(
            @PathVariable("id") Long id,
            @RequestParam("quantity") Integer quantity,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    );

}