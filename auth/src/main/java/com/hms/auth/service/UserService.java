package com.hms.auth.service;

import com.hms.auth.dto.SignupRequest;
import com.hms.auth.dto.UpdateUserRequest;
import com.hms.auth.dto.UserResponse;
import com.hms.auth.model.Role;
import com.hms.auth.model.RoleName;
import com.hms.auth.model.User;
import com.hms.auth.repository.RoleRepository;
import com.hms.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public User createUser(SignupRequest signUpRequest) {
        // Create new user account
        User user = new User(signUpRequest.getUsername(),
                           signUpRequest.getEmail(),
                           passwordEncoder.encode(signUpRequest.getPassword()),
                           signUpRequest.getFirstName(),
                           signUpRequest.getLastName());

        user.setPhoneNumber(signUpRequest.getPhoneNumber());

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();
        boolean isPatient = false;

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(RoleName.ROLE_PATIENT)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
            isPatient = true;
        } else {
            // Check if patient role is in the roles before processing
            boolean containsPatient = strRoles.contains("patient") || strRoles.stream().noneMatch(role -> 
                role.equals("admin") || role.equals("doctor") || role.equals("nurse"));
            
            for (String role : strRoles) {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "doctor":
                        Role doctorRole = roleRepository.findByName(RoleName.ROLE_DOCTOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(doctorRole);
                        break;
                    case "nurse":
                        Role nurseRole = roleRepository.findByName(RoleName.ROLE_NURSE)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(nurseRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(RoleName.ROLE_PATIENT)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                        break;
                }
            }
            
            isPatient = containsPatient || roles.stream().anyMatch(role -> role.getName() == RoleName.ROLE_PATIENT);
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        // Send Kafka event when user is created with proper JSON format
        try {
            String userEventData = String.format(
                "{\"userId\":%d,\"username\":\"%s\",\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"phoneNumber\":\"%s\",\"roles\":[%s]}", 
                savedUser.getId(), 
                savedUser.getUsername(), 
                savedUser.getEmail(), 
                savedUser.getFirstName(), 
                savedUser.getLastName(), 
                savedUser.getPhoneNumber() != null ? savedUser.getPhoneNumber() : "",
                savedUser.getRoles().stream()
                    .map(role -> "\"" + role.getName().name() + "\"")
                    .collect(Collectors.joining(","))
            );
            kafkaTemplate.send("user.created", userEventData);
        } catch (Exception e) {
            logger.warn("Failed to send user.created Kafka event: {}", e.getMessage());
        }
        
        // Send specific event for patient registration
        if (isPatient) {
            try {
                String patientData = String.format(
                    "{\"userId\":%d,\"username\":\"%s\",\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"phoneNumber\":\"%s\"}", 
                    savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), 
                    savedUser.getFirstName(), savedUser.getLastName(), 
                    savedUser.getPhoneNumber() != null ? savedUser.getPhoneNumber() : ""
                );
                kafkaTemplate.send("user.patient.created", patientData);
            } catch (Exception e) {
                logger.warn("Failed to send user.patient.created Kafka event: {}", e.getMessage());
            }
        }

        return savedUser;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToUserResponse);
    }

    public List<UserResponse> getUsersByRole(String roleName) {
        return userRepository.findByRoleName("ROLE_" + roleName.toUpperCase()).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public User updateUser(Long id, UpdateUserRequest updateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Only update fields that are provided (not null)
        if (updateRequest.getFirstName() != null && !updateRequest.getFirstName().trim().isEmpty()) {
            user.setFirstName(updateRequest.getFirstName().trim());
        }
        
        if (updateRequest.getLastName() != null && !updateRequest.getLastName().trim().isEmpty()) {
            user.setLastName(updateRequest.getLastName().trim());
        }
        
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().trim().isEmpty()) {
            // Check if email is already taken by another user
            Optional<User> existingUser = userRepository.findByEmail(updateRequest.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw new RuntimeException("Email is already in use by another user");
            }
            user.setEmail(updateRequest.getEmail().trim());
        }
        
        if (updateRequest.getPhoneNumber() != null && !updateRequest.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(updateRequest.getPhoneNumber().trim());
        }
        
        if (updateRequest.getIsActive() != null) {
            user.setIsActive(updateRequest.getIsActive());
        }

        return userRepository.save(user);
    }

    // Keep the old method for backward compatibility but mark as deprecated
    @Deprecated
    public User updateUser(Long id, User userDetails) {
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setFirstName(userDetails.getFirstName());
        updateRequest.setLastName(userDetails.getLastName());
        updateRequest.setEmail(userDetails.getEmail());
        updateRequest.setPhoneNumber(userDetails.getPhoneNumber());
        updateRequest.setIsActive(userDetails.getIsActive());
        
        return updateUser(id, updateRequest);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        userRepository.delete(user);
    }

    public Optional<UserResponse> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToUserResponse);
    }

    private UserResponse convertToUserResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getIsActive(),
                user.getIsEmailVerified(),
                user.getCreatedAt(),
                roles
        );
    }
}
