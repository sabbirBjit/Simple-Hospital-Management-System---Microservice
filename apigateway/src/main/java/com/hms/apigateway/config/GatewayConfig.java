package com.hms.apigateway.config;

import com.hms.apigateway.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth service routes - no authentication required
                .route("auth-service", r -> r.path("/api/auth/**")
                        .uri("http://localhost:8081"))
                
                // User management routes - authentication required
                .route("user-service", r -> r.path("/api/users/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("http://localhost:8081"))
                
                // Protected routes - authentication required
                .route("patient-service", r -> r.path("/api/patients/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("http://localhost:8082"))
                
                .route("appointment-service", r -> r.path("/api/appointments/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("http://localhost:8083"))
                
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("http://localhost:8084"))
                
                .build();
    }
}
