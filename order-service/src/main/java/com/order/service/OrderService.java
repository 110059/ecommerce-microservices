package com.order.service;

import com.order.dto.OrderRequest;
import com.order.dto.OrderResponse;
import com.order.dto.ProductResponse;
import com.order.entity.Order;
import com.order.exception.InsufficientStockException;
import com.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class OrderService {
    private static final Logger log =
            LoggerFactory.getLogger(OrderService.class);
//    INFO → normal business events.
//    DEBUG → detailed logs for your code.
//    WARN → potential problems.
//    ERROR → failures.

    private final OrderRepository repository;
    private final ProductClient productClient;

    public OrderService(OrderRepository repository, ProductClient productClient) {
        this.repository = repository;
        this.productClient = productClient;
    }

    @CircuitBreaker(
            name = "productService",
            fallbackMethod = "productServiceFallback"
    )
    public OrderResponse createOrder(OrderRequest request) {

        Order order = new Order();

        order.setUserId(request.getUserId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());

        // Temporary calculation
        //order.setTotalPrice(request.getQuantity() * 100.0);

        ProductResponse product = productClient.getProduct(request.getProductId());


        if (product.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient Stock");
        }

        productClient.reduceStock(request.getProductId(), request.getQuantity());

        double totalPrice = product.getPrice() * request.getQuantity();

        order.setTotalPrice(totalPrice);

        order.setStatus("PLACED");

        Order savedOrder = repository.save(order);

        log.info(" order created for user {}", request.getUserId());

        return mapToResponse(savedOrder);
    }

    public OrderResponse productServiceFallback(
            OrderRequest request,
            Throwable throwable) {

        log.error(
                "Product Service unavailable. productId={}, error={}",
                request.getProductId(),
                throwable.getMessage()
        );

        return new OrderResponse(
                null,
                request.getUserId(),
                request.getProductId(),
                request.getQuantity(),
                0.0,
                "PRODUCT_SERVICE_UNAVAILABLE"
        );
    }


    public List<OrderResponse> getAllOrders() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    public OrderResponse getOrder(Long id) {

        Order order = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        return mapToResponse(order);

    }

    public void deleteOrder(Long id) {

        repository.deleteById(id);

    }

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