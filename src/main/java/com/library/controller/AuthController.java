package com.library.controller;

import com.library.dto.AuthDto;
import com.library.model.User;
import com.library.repository.UserRepository;
import com.library.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDto.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(
                    AuthDto.ApiResponse.builder()
                            .success(false)
                            .message("Invalid username or password")
                            .build()
            );
        }

        if (!user.isActive()) {
            return ResponseEntity.status(403).body(
                    AuthDto.ApiResponse.builder()
                            .success(false)
                            .message("Account is inactive. Please contact admin.")
                            .build()
            );
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.isAdmin());

        return ResponseEntity.ok(
                AuthDto.LoginResponse.builder()
                        .token(token)
                        .username(user.getUsername())
                        .name(user.getName())
                        .isAdmin(user.isAdmin())
                        .message("Login successful")
                        .build()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body(
                    AuthDto.ApiResponse.builder().success(false).message("User not found").build()
            );
        }

        return ResponseEntity.ok(
                AuthDto.LoginResponse.builder()
                        .username(user.getUsername())
                        .name(user.getName())
                        .isAdmin(user.isAdmin())
                        .build()
        );
    }
}