package com.order.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/*
opemfeign: synchronous communication  (kafkaTemplate asynchromous communication)
this is used when using feign client instead of clientTemplate
to avoid Problems:

        Manual URL creation
        More code
        Harder to maintain
        More boilerplate
*/
@FeignClient(name = "USER-SERVICE")
public interface UserClient {
    @GetMapping("/users/{id}")
    String getUser(@PathVariable int id);
}
