package com.hms.appointment.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
               .parseClaimsJws(token).getBody().getSubject();
    }

    public String getUserIdFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key()).build()
                   .parseClaimsJws(token).getBody();
            Long userId = claims.get("userId", Long.class);
            return userId != null ? userId.toString() : null;
        } catch (Exception e) {
            logger.error("Cannot extract userId from JWT token: {}", e.getMessage());
            return null;
        }
    }

    public String getEmailFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key()).build()
                   .parseClaimsJws(token).getBody();
            return claims.get("email", String.class);
        } catch (Exception e) {
            logger.error("Cannot extract email from JWT token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
