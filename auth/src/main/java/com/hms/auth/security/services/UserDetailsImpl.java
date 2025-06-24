package com.hms.auth.security.services;

import com.hms.auth.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsImpl.class);

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(User user) {
        if (user == null) {
            logger.error("Cannot build UserDetailsImpl from null user");
            throw new IllegalArgumentException("User cannot be null");
        }
        
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            logger.warn("User {} has no roles assigned", user.getUsername());
        }
        
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    logger.debug("Adding authority: {}", role.getName().name());
                    return new SimpleGrantedAuthority(role.getName().name());
                })
                .collect(Collectors.toList());

        logger.debug("Built UserDetailsImpl for user: {} with authorities: {}", 
                user.getUsername(), authorities);

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPassword(),
                authorities);
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
