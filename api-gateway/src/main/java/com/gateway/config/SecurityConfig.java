package com.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * this file required as security is added as security
 * and to avoid login form, and bypass the request to other webservices
 * this security config is required.
 */
@Configuration
public class SecurityConfig {

        @Bean
        public SecurityWebFilterChain securityFilterChain(
                ServerHttpSecurity http) {

            http
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)

                    .authorizeExchange(exchange -> exchange

                            .anyExchange()
                            .permitAll()
                    )

                    .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                    .formLogin(ServerHttpSecurity.FormLoginSpec::disable);

            return http.build();
        }

}