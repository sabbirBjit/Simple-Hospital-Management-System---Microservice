package com.hms.apigateway.filter;

import com.hms.apigateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Skip authentication for auth endpoints
            if (isAuthEndpoint(request)) {
                return chain.filter(exchange);
            }

            // Check for JWT token
            String token = extractToken(request);
            
            if (token == null) {
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            if (!jwtUtil.validateJwtToken(token)) {
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // Add user info to headers for downstream services
            String username = jwtUtil.getUserNameFromJwtToken(token);
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Name", username)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private boolean isAuthEndpoint(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return path.contains("/api/auth/") || 
               path.equals("/actuator/health") ||
               path.startsWith("/actuator/");
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        logger.error("Authentication error: {}", err);
        return response.setComplete();
    }

    public static class Config {
        // Configuration properties if needed
    }
}
