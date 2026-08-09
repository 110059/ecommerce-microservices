package com.order.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    @LoadBalanced // Because Eureka service name will be used: ex    http://USER-SERVICE/users/101
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}