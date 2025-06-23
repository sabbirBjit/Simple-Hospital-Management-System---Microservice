package com.hms.auth.config;

import com.hms.auth.model.Role;
import com.hms.auth.model.RoleName;
import com.hms.auth.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(RoleName.ROLE_ADMIN, "System Administrator"));
            roleRepository.save(new Role(RoleName.ROLE_DOCTOR, "Medical Doctor"));
            roleRepository.save(new Role(RoleName.ROLE_NURSE, "Registered Nurse"));
            roleRepository.save(new Role(RoleName.ROLE_PATIENT, "Patient"));
            
            System.out.println("Default roles created successfully!");
        }
    }
}
