package com.hms.appointment.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String username = request.getHeader("X-User-Name");
            String userId = request.getHeader("X-User-Id");
            
            if (StringUtils.hasText(username) && StringUtils.hasText(userId)) {
                // Create a simple authentication object with minimal roles
                // In a real scenario, you might want to fetch user roles from the auth service
                List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_ADMIN") // Assuming admin from the token
                );
                
                UserPrincipal userPrincipal = new UserPrincipal(Long.valueOf(userId), username);
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Set authentication for user: {} with ID: {}", username, userId);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication from headers: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
    
    public static class UserPrincipal {
        private final Long id;
        private final String username;
        
        public UserPrincipal(Long id, String username) {
            this.id = id;
            this.username = username;
        }
        
        public Long getId() { return id; }
        public String getUsername() { return username; }
    }
}
