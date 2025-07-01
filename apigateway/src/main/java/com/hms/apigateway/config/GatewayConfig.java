package com.hms.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth service routes - no authentication required (handled by global filter)
                .route("auth-service", r -> r.path("/api/auth/**")
                        .uri("http://localhost:8081"))
                
                // User management routes - authentication handled by global filter
                .route("user-service", r -> r.path("/api/users/**")
                        .uri("http://localhost:8081"))
                
                // Protected routes - authentication handled by global filter
                .route("patient-service", r -> r.path("/api/patients/**")
                        .uri("http://localhost:8082"))
                
                .route("appointment-service", r -> r.path("/api/appointments/**")
                        .uri("http://localhost:8083"))
                
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .uri("http://localhost:8084"))
                
                .build();
    }
}
