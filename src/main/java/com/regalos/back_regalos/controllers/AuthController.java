package com.regalos.back_regalos.controllers;

import com.regalos.back_regalos.config.JwtUtils;
import com.regalos.back_regalos.dtos.LoginRequest;
import com.regalos.back_regalos.dtos.LoginResponse;
import com.regalos.back_regalos.models.AppUser;
import com.regalos.back_regalos.repositories.AppUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para Login y Logout")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final AppUserRepository userRepository;

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve un token JWT")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        final String jwt = jwtUtils.generateToken(userDetails);
        
        AppUser user = userRepository.findByEmail(request.getEmail()).get();

        return ResponseEntity.ok(LoginResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .role(user.getRole().getName())
                .build());
    }
}
