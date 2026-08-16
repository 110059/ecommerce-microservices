package com.order.service;

import com.order.config.FeignConfig;
import com.order.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "USER-SERVICE",
        configuration = FeignConfig.class
)
public interface UserClient {

    @GetMapping("/users/{id}")
    UserResponse getUser(@PathVariable("id") Long id);
}