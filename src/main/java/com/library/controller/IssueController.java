package com.library.controller;

import com.library.dto.AuthDto.ApiResponse;
import com.library.model.Book;
import com.library.model.Issue;
import com.library.repository.BookRepository;
import com.library.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueRepository issueRepository;
    private final BookRepository bookRepository;

    @PostMapping("/issue")
    public ResponseEntity<?> issueBook(@RequestBody Map<String, Object> req) {
        String serialNo    = (String) req.get("bookSerialNo");
        String membershipId = (String) req.get("membershipId");
        String remarks     = (String) req.getOrDefault("remarks", "");
        String issueDateStr = (String) req.get("issueDate");
        String returnDateStr = (String) req.get("returnDate");

        if (serialNo == null || membershipId == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().success(false).message("Book serial number and membership ID are required").build()
            );
        }

        Book book = bookRepository.findBySerialNo(serialNo).orElse(null);
        if (book == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().success(false).message("Book not found with serial: " + serialNo).build()
            );
        }
        if (!"AVAILABLE".equals(book.getStatus())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().success(false).message("Book is not available for issue").build()
            );
        }

        boolean alreadyIssued = issueRepository
                .findByMembershipIdAndStatus(membershipId, "ISSUED")
                .stream().anyMatch(i -> i.getBookSerialNo().equals(serialNo));
        if (alreadyIssued) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().success(false).message("This book is already issued to this member").build()
            );
        }

        LocalDate issueDate  = issueDateStr != null ? LocalDate.parse(issueDateStr) : LocalDate.now();
        LocalDate returnDate = returnDateStr != null ? LocalDate.parse(returnDateStr) : issueDate.plusDays(15);

        if (issueDate.isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().success(false).message("Issue date cannot be in the past").build()
            );
        }
        if (returnDate.isAfter(issueDate.plusDays(15))) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().success(false).message("Return date cannot be more than 15 days from issue date").build()
            );
        }

        Issue issue = Issue.builder()
                .bookSerialNo(serialNo)
                .bookName(book.getName())
                .authorName(book.getAuthorName())
                .membershipId(membershipId)
                .issueDate(issueDate)
                .expectedReturnDate(returnDate)
                .status("ISSUED")
                .fineCalculated(0.0)
                .finePaid(false)
                .remarks(remarks)
                .itemType(book.getType())
                .build();

        issueRepository.save(issue);

        // Mark book as issued
        book.setStatus("ISSUED");
        bookRepository.save(book);

        return ResponseEntity.ok(
                ApiResponse.builder().success(true).message("Book issued successfully").data(issue).build()
        );
    }

    @PostMapping("/return")
    public ResponseEntity<?> returnBook(@RequestBody Map<String, Object> req) {
        String serialNo    = (String) req.get("bookSerialNo");
        String returnDateStr = (String) req.get("actualReturnDate");
        String remarks     = (String) req.getOrDefault("remarks", "");

        if (serialNo == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().success(false).message("Book serial number is required").build()
            );
        }

        Issue issue = issueRepository.findByBookSerialNoAndStatus(serialNo, "ISSUED").orElse(null);
        if (issue == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().success(false).message("No active issue found for this book").build()
            );
        }

        LocalDate actualReturnDate = returnDateStr != null
                ? LocalDate.parse(returnDateStr) : LocalDate.now();

        double fine = 0.0;
        if (actualReturnDate.isAfter(issue.getExpectedReturnDate())) {
            long overdueDays = ChronoUnit.DAYS.between(issue.getExpectedReturnDate(), actualReturnDate);
            fine = overdueDays * 5.0;
        }

        issue.setActualReturnDate(actualReturnDate);
        issue.setFineCalculated(fine);
        issue.setRemarks(remarks);
        issue.setStatus(fine > 0 ? "PENDING_FINE" : "PENDING_RETURN");
        issueRepository.save(issue);

        return ResponseEntity.ok(
                ApiResponse.builder().success(true).message("Proceed to pay fine screen").data(issue).build()
        );
    }

    @PostMapping("/pay-fine/{issueId}")
    public ResponseEntity<?> payFine(@PathVariable String issueId,
                                     @RequestBody Map<String, Object> req) {
        boolean finePaid = Boolean.TRUE.equals(req.get("finePaid"));
        String remarks   = (String) req.getOrDefault("remarks", "");

        Issue issue = issueRepository.findById(issueId).orElse(null);
        if (issue == null) {
            return ResponseEntity.notFound().build();
        }

        if (issue.getFineCalculated() > 0 && !finePaid) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().success(false)
                            .message("Fine must be paid before completing the return").build()
            );
        }

        issue.setFinePaid(finePaid || issue.getFineCalculated() == 0);
        issue.setStatus("RETURNED");
        if (!remarks.isBlank()) issue.setRemarks(remarks);
        issueRepository.save(issue);

        bookRepository.findBySerialNo(issue.getBookSerialNo()).ifPresent(book -> {
            book.setStatus("AVAILABLE");
            bookRepository.save(book);
        });

        return ResponseEntity.ok(
                ApiResponse.builder().success(true).message("Book returned successfully").data(issue).build()
        );
    }

    @GetMapping("/by-serial/{serialNo}")
    public ResponseEntity<?> getIssueBySerial(@PathVariable String serialNo) {
        return issueRepository.findByBookSerialNoAndStatus(serialNo, "ISSUED")
                .or(() -> issueRepository.findByBookSerialNoAndStatus(serialNo, "PENDING_FINE"))
                .or(() -> issueRepository.findByBookSerialNoAndStatus(serialNo, "PENDING_RETURN"))
                .map(issue -> ResponseEntity.ok(ApiResponse.builder().success(true).data(issue).build()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveIssues() {
        List<Issue> active = issueRepository.findByStatus("ISSUED");
        return ResponseEntity.ok(ApiResponse.builder().success(true).data(active).build());
    }

    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueIssues() {
        List<Issue> overdue = issueRepository
                .findByExpectedReturnDateBeforeAndStatus(LocalDate.now(), "ISSUED");
        return ResponseEntity.ok(ApiResponse.builder().success(true).data(overdue).build());
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingIssues() {
        List<Issue> pending = issueRepository.findByStatus("PENDING_FINE");
        pending.addAll(issueRepository.findByStatus("PENDING_RETURN"));
        return ResponseEntity.ok(ApiResponse.builder().success(true).data(pending).build());
    }
}
