package com.product.service;

import com.product.dto.ProductRequest;
import com.product.dto.ProductResponse;
import com.product.dto.StockReservedEvent;
import com.product.kafka.StockEventProducer;
import com.product.exception.ProductNotFoundException;
import com.product.model.ProcessedRequest;
import com.product.model.Product;
import com.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.product.repository.ProcessedRequestRepository;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ProductService {

    private final ProductRepository repository;
    private final ProcessedRequestRepository processedRequestRepository;

    private final StockEventProducer stockEventProducer;

    public ProductService(
            ProductRepository repository,
            ProcessedRequestRepository processedRequestRepository,
            StockEventProducer stockEventProducer) {

        this.repository = repository;
        this.processedRequestRepository = processedRequestRepository;
        this.stockEventProducer = stockEventProducer;
    }

    // Create Product
    public ProductResponse save(ProductRequest request) {

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());

        Product saved = repository.save(product);

        return mapToResponse(saved);
    }

    // Get All Products
    public List<ProductResponse> getAll() {

        return repository.findAll()

                .stream()

                .map(this::mapToResponse)

                .collect(Collectors.toList());
    }

    // Get Product By id
    public ProductResponse getById(Long id) {

        Product product = repository.findById(id)

                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id : " + id));

        return mapToResponse(product);
    }

    // Update Product
    public ProductResponse update(
            Long id,
            ProductRequest request) {

        Product product = repository.findById(id)

                .orElseThrow(() ->  new ProductNotFoundException(
                        "Product not found with id : " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());

        Product updated = repository.save(product);

        return mapToResponse(updated);
    }

    // Delete Product
    public void delete(Long id) {

        if (!repository.existsById(id)) {

            throw new ProductNotFoundException("Product not found with id : " + id);
        }

        repository.deleteById(id);
    }

    @Transactional
    public void reduceStock(
            Long id,
            Integer quantity,
            Long orderId,
            Long userId,
            Double totalPrice,
            String idempotencyKey) {

        // Request already processed
        if (processedRequestRepository
                .existsByIdempotencyKey(idempotencyKey)) {

            return;
        }

        Product product = repository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found")
                );

        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient Stock");
        }

        product.setQuantity(
                product.getQuantity() - quantity);

        repository.save(product);

        // Mark request as processed
        ProcessedRequest processedRequest = new ProcessedRequest();
        processedRequest.setIdempotencyKey(idempotencyKey);

        processedRequestRepository.save(processedRequest);

        // Publish Saga event
        StockReservedEvent event = new StockReservedEvent(
                orderId,
                userId,
                id,
                quantity,
                totalPrice
        );

        stockEventProducer.sendStockReserved(event);
    }

    @Transactional
    public void restoreStock(Long id, Integer quantity) {

        Product product = repository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id : " + id));

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0");
        }

        product.setQuantity(
                product.getQuantity() + quantity);

        repository.save(product);
    }

    // Entity -> Response DTO
    private ProductResponse mapToResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity()
        );
    }
}