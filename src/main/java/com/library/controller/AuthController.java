package com.library.controller;

import com.library.dto.AuthDto;
import com.library.model.Membership;
import com.library.model.User;
import com.library.repository.MembershipRepository;
import com.library.repository.UserRepository;
import com.library.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDto.LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

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
                    AuthDto.ApiResponse.builder()
                            .success(false)
                            .message("User not found")
                            .build()
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


    @PostMapping("/member-signup")
    public ResponseEntity<?> memberSignup(@RequestBody AuthDto.MemberSignupRequest request) {

        if (request.getMembershipId() == null || request.getMembershipId().isBlank()
                || request.getName() == null || request.getName().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(
                    AuthDto.ApiResponse.builder()
                            .success(false)
                            .message("Sabhi fields required hain.")
                            .build()
            );
        }

        if (request.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body(
                    AuthDto.ApiResponse.builder()
                            .success(false)
                            .message("Password kam se kam 6 characters ka hona chahiye.")
                            .build()
            );
        }


        Optional<Membership> existing = membershipRepository
                .findByMembershipId(request.getMembershipId().toUpperCase());

        if (existing.isPresent() && existing.get().getPasswordHash() != null) {
            return ResponseEntity.status(409).body(
                    AuthDto.ApiResponse.builder()
                            .success(false)
                            .message("Is Membership ID pe account pehle se bana hua hai.")
                            .build()
            );
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());

        if (existing.isPresent()) {
            // Record hai but password nahi — sirf password set karo
            Membership member = existing.get();
            member.setPasswordHash(passwordHash);
            membershipRepository.save(member);
        } else {

            String membershipId = request.getMembershipId().toUpperCase();


            String[] nameParts = request.getName().trim().split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            Membership newMember = Membership.builder()
                    .membershipId(membershipId)
                    .firstName(firstName)
                    .lastName(lastName)
                    .passwordHash(passwordHash)
                    .status("ACTIVE")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusYears(1))
                    .membershipType("ONE_YEAR")
                    .pendingFine(0.0)
                    .build();

            membershipRepository.save(newMember);
        }

        return ResponseEntity.status(201).body(
                AuthDto.ApiResponse.builder()
                        .success(true)
                        .message("Account successfully Created . Now! you can login.")
                        .build()
        );
    }

    @PostMapping("/member-login")
    public ResponseEntity<?> memberLogin(@RequestBody AuthDto.MemberLoginRequest request) {

        if (request.getMembershipId() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(
                    AuthDto.ApiResponse.builder()
                            .success(false)
                            .message("Membership ID and password are required.")
                            .build()
            );
        }

        Membership member = membershipRepository
                .findByMembershipId(request.getMembershipId().toUpperCase())
                .orElse(null);

        if (member == null) {
            return ResponseEntity.status(401).body(
                    AuthDto.ApiResponse.builder()
                            .success(false)
                            .message("Member not found.")
                            .build()
            );
        }

        if (member.getPasswordHash() == null) {
            return ResponseEntity.status(401).body(
                    AuthDto.ApiResponse.builder()
                            .success(false)
                            .message("Pehle signup karo.")
                            .build()
            );
        }

        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            return ResponseEntity.status(401).body(
                    AuthDto.ApiResponse.builder()
                            .success(false)
                            .message("Invalid credentials.")
                            .build()
            );
        }

        if ("INACTIVE".equals(member.getStatus()) || "CANCELLED".equals(member.getStatus())) {
            return ResponseEntity.status(403).body(
                    AuthDto.ApiResponse.builder()
                            .success(false)
                            .message("Membership inactive/cancelled hai. Admin se contact karo.")
                            .build()
            );
        }

        String token = jwtUtil.generateToken(member.getMembershipId(), false);

        String fullName = ((member.getFirstName() != null ? member.getFirstName() : "")
                + " " +
                (member.getLastName() != null ? member.getLastName() : "")).trim();

        return ResponseEntity.ok(
                AuthDto.MemberLoginResponse.builder()
                        .token(token)
                        .membershipId(member.getMembershipId())
                        .name(fullName)
                        .message("Login successful")
                        .build()
        );
    }
}