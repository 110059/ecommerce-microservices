package com.order.controller;

import com.order.dto.OrderRequest;
import com.order.dto.OrderResponse;
import com.order.dto.UserResponse;
import com.order.service.OrderEventProducer;
import com.order.service.OrderService;
import com.order.service.UserClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderEventProducer eventProducer;
    private final UserClient userClient;

    public OrderController(OrderService orderService,
                           OrderEventProducer eventProducer,
                           UserClient userClient) {
        this.orderService = orderService;
        this.eventProducer = eventProducer;
        this.userClient = userClient;
    }

    // ============================
    // CRUD APIs
    // ============================

    @PostMapping
    public OrderResponse createOrder(
            @Valid @RequestBody OrderRequest request) {

        OrderResponse response = orderService.createOrder(request);


        if ("PLACED".equals(response.getStatus())) {
            eventProducer.sendOrderEvent(response);
        }

        return response;
    }


    @PutMapping("/{id}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable Long id) {

        OrderResponse response =
                orderService.cancelOrder(id);

        eventProducer.sendOrderCancelledEvent(response);


        return response;
    }


    @GetMapping("/all")
    public List<OrderResponse> getAllOrders() {

        return orderService.getAllOrders();

    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(
            @PathVariable Long id) {

        return orderService.getOrder(id);

    }

    @DeleteMapping("/{id}")
    public String deleteOrder(
            @PathVariable Long id) {

        orderService.deleteOrder(id);

        return "Order Deleted Successfully";

    }

    // ============================
    // Demo APIs
    // ============================

    @GetMapping("/async")
    public void orderAsync() {
        eventProducer.sendOrderEvent(new OrderResponse());

    }

    @GetMapping("/demo/{id}")
    @CircuitBreaker(
            name = "userService",
            fallbackMethod = "fallbackResponse")
    public String orderSync(
            @PathVariable int id) {

        UserResponse userResponse = userClient.getUser(id);

        eventProducer.sendOrderEvent(new OrderResponse());

        return "Order Created For User -> "
                + userResponse;

    }

    public String fallbackResponse(
            int id,
            Exception ex) {

        return "User Service is temporarily unavailable";

    }

}