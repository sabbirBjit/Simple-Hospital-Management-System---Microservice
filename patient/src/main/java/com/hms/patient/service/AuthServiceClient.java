package com.hms.patient.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class AuthServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceClient.class);
    
    @Value("${services.auth-service.url}")
    private String authServiceUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Get user details by username
     */
    public Optional<UserDto> getUserByUsername(String username) {
        try {
            String url = authServiceUrl + "/api/users/username/" + username;
            logger.info("Calling auth service at: {}", url);
            
            UserDto user = restTemplate.getForObject(url, UserDto.class);
            return Optional.ofNullable(user);
            
        } catch (Exception e) {
            logger.error("Error calling auth service for username {}: {}", username, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Get user details by user ID
     */
    public Optional<UserDto> getUserById(Long userId) {
        try {
            String url = authServiceUrl + "/api/users/" + userId;
            logger.info("Calling auth service at: {}", url);
            
            UserDto user = restTemplate.getForObject(url, UserDto.class);
            return Optional.ofNullable(user);
            
        } catch (Exception e) {
            logger.error("Error calling auth service for userId {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Simple DTO for user data
     */
    public static class UserDto {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }
}
