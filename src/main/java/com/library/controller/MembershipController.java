package com.library.controller;

import com.library.dto.AuthDto.ApiResponse;
import com.library.model.Membership;
import com.library.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipRepository membershipRepository;

    // Get all memberships
    @GetMapping
    public ResponseEntity<?> getAllMemberships() {
        List<Membership> list = membershipRepository.findAll();
        return ResponseEntity.ok(ApiResponse.builder().success(true).data(list).build());
    }

    // Get active memberships
    @GetMapping("/active")
    public ResponseEntity<?> getActiveMemberships() {
        List<Membership> list = membershipRepository.findByStatus("ACTIVE");
        return ResponseEntity.ok(ApiResponse.builder().success(true).data(list).build());
    }

    // Get by membership ID
    @GetMapping("/{membershipId}")
    public ResponseEntity<?> getByMembershipId(@PathVariable String membershipId) {
        return membershipRepository.findByMembershipId(membershipId)
                .map(m -> ResponseEntity.ok(ApiResponse.builder().success(true).data(m).build()))
                .orElse(ResponseEntity.notFound().build());
    }

    // Add new membership (Admin only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addMembership(@RequestBody Membership membership) {
        // Validate all required fields
        if (membership.getFirstName() == null || membership.getFirstName().isBlank() ||
                membership.getLastName() == null || membership.getLastName().isBlank() ||
                membership.getContactNumber() == null || membership.getContactNumber().isBlank() ||
                membership.getAadharCardNo() == null || membership.getAadharCardNo().isBlank() ||
                membership.getMembershipType() == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().success(false).message("All fields are required").build()
            );
        }

        // Auto-generate membership ID
        long count = membershipRepository.count();
        String membershipId = String.format("MEM%05d", count + 1);

        LocalDate startDate = membership.getStartDate() != null
                ? membership.getStartDate() : LocalDate.now();
        LocalDate endDate = calculateEndDate(startDate, membership.getMembershipType());

        membership.setMembershipId(membershipId);
        membership.setStartDate(startDate);
        membership.setEndDate(endDate);
        membership.setStatus("ACTIVE");
        membership.setPendingFine(0.0);

        membershipRepository.save(membership);
        return ResponseEntity.ok(
                ApiResponse.builder().success(true)
                        .message("Membership created: " + membershipId).data(membership).build()
        );
    }

    // Update membership (Admin only) — extend or cancel
    @PutMapping("/{membershipId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateMembership(@PathVariable String membershipId,
                                              @RequestBody Map<String, Object> req) {
        Membership membership = membershipRepository.findByMembershipId(membershipId).orElse(null);
        if (membership == null) {
            return ResponseEntity.notFound().build();
        }

        String action = (String) req.get("action"); // EXTEND or CANCEL
        String extensionType = (String) req.get("extensionType");

        if ("CANCEL".equals(action)) {
            membership.setStatus("CANCELLED");
            membership.setEndDate(LocalDate.now());
        } else if ("EXTEND".equals(action) && extensionType != null) {
            LocalDate baseDate = membership.getEndDate().isAfter(LocalDate.now())
                    ? membership.getEndDate() : LocalDate.now();
            membership.setEndDate(calculateEndDate(baseDate, extensionType));
            membership.setMembershipType(extensionType);
            membership.setStatus("ACTIVE");
        }

        membershipRepository.save(membership);
        return ResponseEntity.ok(
                ApiResponse.builder().success(true).message("Membership updated").data(membership).build()
        );
    }

    private LocalDate calculateEndDate(LocalDate startDate, String type) {
        return switch (type) {
            case "SIX_MONTHS" -> startDate.plusMonths(6);
            case "ONE_YEAR"   -> startDate.plusYears(1);
            case "TWO_YEARS"  -> startDate.plusYears(2);
            default           -> startDate.plusMonths(6);
        };
    }
}