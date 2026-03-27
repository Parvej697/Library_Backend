package com.library.controller;

import com.library.dto.AuthDto.ApiResponse;
import com.library.model.User;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        // Clear passwords before sending
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(ApiResponse.builder().success(true).data(users).build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> req) {
        String username = (String) req.get("username");
        String name     = (String) req.get("name");
        String password = (String) req.getOrDefault("password", "user123");
        boolean isAdmin = Boolean.TRUE.equals(req.get("isAdmin"));
        boolean isActive = !req.containsKey("isActive") || Boolean.TRUE.equals(req.get("isActive"));

        if (username == null || username.isBlank() || name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().success(false).message("Username and name are required").build()
            );
        }

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().success(false).message("Username already exists").build()
            );
        }

        User user = User.builder()
                .username(username)
                .name(name)
                .password(passwordEncoder.encode(password))
                .isAdmin(isAdmin)
                .isActive(isActive)
                .build();

        userRepository.save(user);
        user.setPassword(null);
        return ResponseEntity.ok(
                ApiResponse.builder().success(true).message("User created successfully").data(user).build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id,
                                        @RequestBody Map<String, Object> req) {
        return userRepository.findById(id).map(user -> {
            if (req.containsKey("name"))     user.setName((String) req.get("name"));
            if (req.containsKey("isAdmin"))  user.setAdmin(Boolean.TRUE.equals(req.get("isAdmin")));
            if (req.containsKey("isActive")) user.setActive(Boolean.TRUE.equals(req.get("isActive")));
            if (req.containsKey("password")) {
                user.setPassword(passwordEncoder.encode((String) req.get("password")));
            }
            userRepository.save(user);
            user.setPassword(null);
            return ResponseEntity.ok(
                    ApiResponse.builder().success(true).message("User updated").data(user).build()
            );
        }).orElse(ResponseEntity.notFound().build());
    }
}