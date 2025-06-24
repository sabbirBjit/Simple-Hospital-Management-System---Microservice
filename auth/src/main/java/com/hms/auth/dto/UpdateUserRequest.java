package com.hms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    @Size(max = 50, message = "Email must not exceed 50 characters")
    @Email(message = "Please provide a valid email address")
    private String email;

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "First name can only contain letters and spaces")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Last name can only contain letters and spaces")
    private String lastName;

    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]*$", message = "Please provide a valid phone number")
    private String phoneNumber;

    private Boolean isActive;
}
