package com.hms.auth.security.jwt;

import com.hms.auth.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs:86400000}")
    private int jwtExpirationMs;

    @Value("${jwt.refreshExpirationMs:604800000}")
    private int refreshExpirationMs;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        
        logger.debug("Generating JWT token for user: {}", userPrincipal.getUsername());

        // Extract roles as a list of strings
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toList());

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .claim("userId", userPrincipal.getId()) // Add userId as claim
                .claim("email", userPrincipal.getEmail())
                .claim("firstName", userPrincipal.getFirstName())
                .claim("lastName", userPrincipal.getLastName())
                .claim("roles", roles) // Add roles as claim
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        
        logger.debug("Generating refresh token for user: {}", userPrincipal.getUsername());

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .claim("userId", userPrincipal.getId()) // Add userId as claim
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + refreshExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        try {
            String username = Jwts.parserBuilder().setSigningKey(key()).build()
                   .parseClaimsJws(token).getBody().getSubject();
            logger.debug("Extracted username from token: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("Cannot extract username from JWT token: {}", e.getMessage());
            throw new RuntimeException("Cannot extract username from JWT token", e);
        }
    }

    public Long getUserIdFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key()).build()
                   .parseClaimsJws(token).getBody();
            Long userId = claims.get("userId", Long.class);
            logger.debug("Extracted userId from token: {}", userId);
            return userId;
        } catch (Exception e) {
            logger.error("Cannot extract userId from JWT token: {}", e.getMessage());
            throw new RuntimeException("Cannot extract userId from JWT token", e);
        }
    }

    public String getEmailFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key()).build()
                   .parseClaimsJws(token).getBody();
            return claims.get("email", String.class);
        } catch (Exception e) {
            logger.error("Cannot extract email from JWT token: {}", e.getMessage());
            throw new RuntimeException("Cannot extract email from JWT token", e);
        }
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            logger.debug("JWT token is valid");
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
