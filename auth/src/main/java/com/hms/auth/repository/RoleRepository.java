package com.hms.auth.repository;

import com.hms.auth.model.Role;
import com.hms.auth.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(RoleName name);
    
    Boolean existsByName(RoleName name);
}
