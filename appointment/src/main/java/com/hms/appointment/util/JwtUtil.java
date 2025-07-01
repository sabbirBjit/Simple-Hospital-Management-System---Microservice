package com.hms.appointment.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtil {
    
    @Value("${app.jwtSecret}")
    private String jwtSecret;
    
    public Long getUserIdFromToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
            
            return Long.valueOf(claims.get("userId", Integer.class));
        } catch (Exception e) {
            return null;
        }
    }
    
    public Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return getUserIdFromToken(token);
    }
    
    public Long getUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            try {
                return Long.valueOf(authentication.getName());
            } catch (NumberFormatException e) {
                // If name is not a number, try to extract from credentials
                if (authentication.getCredentials() instanceof String) {
                    return getUserIdFromToken((String) authentication.getCredentials());
                }
            }
        }
        return null;
    }
}
