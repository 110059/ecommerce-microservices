package com.product.controller;

import com.product.dto.ProductRequest;
import com.product.dto.ProductResponse;
import com.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // Create Product
    @PostMapping
    public ProductResponse createProduct(
            @Valid @RequestBody ProductRequest request) {

        return service.save(request);
    }

    // Get All Products
    @GetMapping
    public List<ProductResponse> getAllProducts() {

        return service.getAll();
    }

    // Get Product By Id
    @GetMapping("/{id}")
    public ProductResponse getProduct(
            @PathVariable Long id) {

        return service.getById(id);
    }

    // Update Product
    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        return service.update(id, request);
    }

    // Delete Product
    @DeleteMapping("/{id}")
    public void deleteProduct(
            @PathVariable Long id) {

        service.delete(id);
    }

    //reduce stock
    @PutMapping("/{id}/reduce-stock")
    public String reduceStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {

        service.reduceStock(id, quantity);

        return "Stock Updated Successfully";
    }
}