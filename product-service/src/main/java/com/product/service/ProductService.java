package com.product.service;

import com.product.dto.ProductRequest;
import com.product.dto.ProductResponse;
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

    public ProductService(
            ProductRepository repository,
            ProcessedRequestRepository processedRequestRepository) {

        this.repository = repository;
        this.processedRequestRepository = processedRequestRepository;
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

    // Get Product By Id
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