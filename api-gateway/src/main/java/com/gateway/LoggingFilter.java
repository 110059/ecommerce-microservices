package com.gateway;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class LoggingFilter {

    @Bean
    public GlobalFilter logFilter() {
        return (exchange, chain) -> {

            System.out.println("Incoming Request: " +
                    exchange.getRequest().getURI());

            return chain.filter(exchange).then(
                    Mono.fromRunnable(() -> {
                        System.out.println("Outgoing Response: " +
                                exchange.getResponse().getStatusCode());
                    })
            );
        };
    }
}