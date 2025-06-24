package com.hms.auth.controller;

import com.hms.auth.dto.ApiResponse;
import com.hms.auth.dto.UserResponse;
import com.hms.auth.dto.UpdateUserRequest;
import com.hms.auth.model.User;
import com.hms.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserResponse> users = userService.getAllUsers();
            return ResponseEntity.ok(new ApiResponse(true, "Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve users: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Optional<UserResponse> user = userService.getUserById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(new ApiResponse(true, "User found", user.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve user: " + e.getMessage()));
        }
    }

    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByRole(@PathVariable String roleName) {
        try {
            List<UserResponse> users = userService.getUsersByRole(roleName);
            return ResponseEntity.ok(new ApiResponse(true, 
                    "Users with role " + roleName + " retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve users by role: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.findByUsername(authentication.name).orElse(null)?.id == #id")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest updateRequest) {
        try {
            User updatedUser = userService.updateUser(id, updateRequest);
            return ResponseEntity.ok(new ApiResponse(true, "User updated successfully", updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to update user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to delete user: " + e.getMessage()));
        }
    }
}
