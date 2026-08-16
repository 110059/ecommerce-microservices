package com.order.controller;

import com.order.dto.OrderRequest;
import com.order.dto.OrderResponse;
import com.order.dto.UserResponse;
import com.order.service.OrderService;
import com.order.service.UserClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@AllArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserClient userClient;


    // ============================
    // CRUD APIs
    // ============================

    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @PutMapping("/{id}/cancel")
    public OrderResponse cancelOrder( @PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    @GetMapping("/all")
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @DeleteMapping("/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return "Order Deleted Successfully";
    }

    // ============================
    // Demo APIs
    // ============================
    @GetMapping("/demo/{id}")
    @CircuitBreaker(
            name = "userService",
            fallbackMethod = "fallbackResponse")
    public String orderSync(@PathVariable Long id) {

        UserResponse userResponse = userClient.getUser(id);
        return "Order Created For User -> " + userResponse;

    }

    public String fallbackResponse(int id,Exception ex) {
        return "User Service is temporarily unavailable";
    }

}