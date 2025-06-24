package com.hms.auth.service;

import com.hms.auth.dto.JwtResponse;
import com.hms.auth.dto.LoginRequest;
import com.hms.auth.security.jwt.JwtUtils;
import com.hms.auth.security.services.UserDetailsImpl;
import com.hms.auth.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new JwtResponse(jwt, refreshToken,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                roles);
    }

    public JwtResponse refreshToken(String refreshToken) {
        if (jwtUtils.validateJwtToken(refreshToken)) {
            String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
            
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            if (!(userDetails instanceof UserDetailsImpl)) {
                throw new RuntimeException("Invalid user details type");
            }
            
            UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
            
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            
            String newToken = jwtUtils.generateJwtToken(authentication);
            String newRefreshToken = jwtUtils.generateRefreshToken(authentication);

            List<String> roles = userDetailsImpl.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return new JwtResponse(newToken, newRefreshToken,
                    userDetailsImpl.getId(),
                    userDetailsImpl.getUsername(),
                    userDetailsImpl.getEmail(),
                    userDetailsImpl.getFirstName(),
                    userDetailsImpl.getLastName(),
                    roles);
        }
        throw new RuntimeException("Invalid refresh token");
    }

    public boolean validateToken(String token) {
        return jwtUtils.validateJwtToken(token);
    }
}
    