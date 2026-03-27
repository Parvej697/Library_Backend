package com.library.controller;

import com.library.dto.AuthDto.ApiResponse;
import com.library.model.Issue;
import com.library.repository.BookRepository;
import com.library.repository.IssueRepository;
import com.library.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final BookRepository bookRepository;
    private final MembershipRepository membershipRepository;
    private final IssueRepository issueRepository;

    @GetMapping("/books")
    public ResponseEntity<?> masterListBooks() {
        return ResponseEntity.ok(ApiResponse.builder().success(true)
                .data(bookRepository.findByType("BOOK")).build());
    }

    @GetMapping("/movies")
    public ResponseEntity<?> masterListMovies() {
        return ResponseEntity.ok(ApiResponse.builder().success(true)
                .data(bookRepository.findByType("MOVIE")).build());
    }

    @GetMapping("/memberships")
    public ResponseEntity<?> masterListMemberships() {
        return ResponseEntity.ok(ApiResponse.builder().success(true)
                .data(membershipRepository.findAll()).build());
    }

    @GetMapping("/active-issues")
    public ResponseEntity<?> activeIssues() {
        List<Issue> issues = issueRepository.findByStatus("ISSUED");
        return ResponseEntity.ok(ApiResponse.builder().success(true).data(issues).build());
    }

    @GetMapping("/overdue")
    public ResponseEntity<?> overdueReturns() {
        List<Issue> overdue = issueRepository
                .findByExpectedReturnDateBeforeAndStatus(LocalDate.now(), "ISSUED");
        // Add fine calculation info
        overdue.forEach(issue -> {
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                    issue.getExpectedReturnDate(), LocalDate.now());
            issue.setFineCalculated(days * 5.0);
        });
        return ResponseEntity.ok(ApiResponse.builder().success(true).data(overdue).build());
    }

    @GetMapping("/issue-requests")
    public ResponseEntity<?> issueRequests() {
        List<Issue> pending = issueRepository.findByStatus("PENDING_FINE");
        pending.addAll(issueRepository.findByStatus("PENDING_RETURN"));
        return ResponseEntity.ok(ApiResponse.builder().success(true).data(pending).build());
    }
}