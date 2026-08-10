package com.order.service;

import com.order.dto.OrderRequest;
import com.order.dto.OrderResponse;
import com.order.dto.ProductResponse;
import com.order.entity.Order;
import com.order.exception.InsufficientStockException;
import com.order.exception.OrderCancellationException;
import com.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log =
            LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository repository;
    private final ProductClient productClient;

    public OrderService(
            OrderRepository repository,
            ProductClient productClient) {

        this.repository = repository;
        this.productClient = productClient;
    }

    // ============================================================
    // CREATE ORDER
    // ============================================================

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        // 1. Idempotency check
        if (request.getIdempotencyKey() != null) {

            Optional<Order> existingOrder =
                    repository.findByIdempotencyKey(
                            request.getIdempotencyKey());

            if (existingOrder.isPresent()) {

                log.info(
                        "Duplicate order request detected. idempotencyKey={}",
                        request.getIdempotencyKey());

                return mapToResponse(existingOrder.get());
            }
        }

        // 2. Get product through protected Product Service call
        ProductResponse product =
                getProductFromProductService(request);

        // 3. Check stock
        //
        // IMPORTANT:
        // This is a business exception.
        // It must NOT go through the Product Service fallback.
        if (product.getQuantity() < request.getQuantity()) {

            throw new InsufficientStockException(
                    "Insufficient Stock. Available: "
                            + product.getQuantity()
                            + ", Requested: "
                            + request.getQuantity()
            );
        }

        // 4. Reduce stock
        reduceProductStock(request);

        // 5. Create order
        Order order = new Order();

        order.setUserId(request.getUserId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setIdempotencyKey(request.getIdempotencyKey());

        // 6. Calculate total price
        double totalPrice =
                product.getPrice() * request.getQuantity();

        order.setTotalPrice(totalPrice);
        order.setStatus("PLACED");

        // 7. Save order
        Order savedOrder = repository.save(order);

        log.info(
                "Order created successfully. userId={}, orderId={}, idempotencyKey={}",
                request.getUserId(),
                savedOrder.getId(),
                request.getIdempotencyKey());

        return mapToResponse(savedOrder);
    }

    // ============================================================
    // GET PRODUCT
    // ============================================================

    @Retry(
            name = "productService",
            fallbackMethod = "productServiceFallback"
    )
    @CircuitBreaker(
            name = "productService",
            fallbackMethod = "productServiceFallback"
    )
    public ProductResponse getProductFromProductService(
            OrderRequest request) {

        return productClient.getProduct(
                request.getProductId());
    }

    // ============================================================
    // PRODUCT SERVICE FALLBACK
    // ============================================================

    public ProductResponse productServiceFallback(
            OrderRequest request,
            Throwable throwable) {

        log.error(
                "Product Service unavailable. productId={}, error={}",
                request.getProductId(),
                throwable.getMessage());

        throw new RuntimeException(
                "Product Service is temporarily unavailable");
    }

    // ============================================================
    // REDUCE STOCK
    // ============================================================

    @Retry(
            name = "productService",
            fallbackMethod = "reduceStockFallback"
    )
    @CircuitBreaker(
            name = "productService",
            fallbackMethod = "reduceStockFallback"
    )
    public void reduceProductStock(
            OrderRequest request) {

        productClient.reduceStock(
                request.getProductId(),
                request.getQuantity(),
                request.getIdempotencyKey());
    }

    // ============================================================
    // REDUCE STOCK FALLBACK
    // ============================================================

    public void reduceStockFallback(
            OrderRequest request,
            Throwable throwable) {

        log.error(
                "Unable to reduce product stock. productId={}, error={}",
                request.getProductId(),
                throwable.getMessage());

        throw new RuntimeException(
                "Product Service is temporarily unavailable while updating stock");
    }

    // ============================================================
    // UPDATE ORDER STATUS TO CANCEL
    // ============================================================

    public OrderResponse cancelOrder(Long id) {

        // 1. Find order
        Order order = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with id : " + id));

        // 2. Prevent duplicate cancellation
        if ("CANCELLED".equals(order.getStatus())) {

            throw new OrderCancellationException(
                    "Order " + id + " is already cancelled");
        }

        // 3. Only placed orders can be cancelled
        if (!"PLACED".equals(order.getStatus())) {

            throw new OrderCancellationException(
                    "Order " + id +
                            " cannot be cancelled. Current status: "
                            + order.getStatus());
        }

        // 4. Restore product stock
        productClient.restoreStock(
                order.getProductId(),
                order.getQuantity()
        );

        // 5. Change order status
        order.setStatus("CANCELLED");

        // 6. Save updated order
        Order updatedOrder = repository.save(order);

        log.info(
                "Order cancelled successfully. orderId={}, productId={}, quantity={}",
                order.getId(),
                order.getProductId(),
                order.getQuantity()
        );

        return mapToResponse(updatedOrder);
    }


    // ============================================================
    // GET ALL ORDERS
    // ============================================================

    public List<OrderResponse> getAllOrders() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ============================================================
    // GET ORDER
    // ============================================================

    public OrderResponse getOrder(Long id) {

        Order order = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found"));

        return mapToResponse(order);
    }

    // ============================================================
    // DELETE ORDER
    // ============================================================

    public void deleteOrder(Long id) {

        repository.deleteById(id);
    }

    // ============================================================
    // ENTITY -> RESPONSE
    // ============================================================

    private OrderResponse mapToResponse(Order order) {

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getProductId(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getStatus());
    }
}
