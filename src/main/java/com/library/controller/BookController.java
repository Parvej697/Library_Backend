package com.library.controller;

import com.library.dto.AuthDto.ApiResponse;
import com.library.model.Book;
import com.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookRepository bookRepository;

    // Search books by name or author (for Book Available screen)
    @GetMapping("/search")
    public ResponseEntity<?> searchBooks(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "BOOK") String type) {

        if ((name == null || name.isBlank()) && (author == null || author.isBlank())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().success(false)
                            .message("Please enter book name or author name to search").build()
            );
        }

        List<Book> books;
        if (name != null && !name.isBlank()) {
            books = bookRepository.findByNameContainingIgnoreCaseAndType(name, type);
        } else {
            books = bookRepository.findByAuthorNameContainingIgnoreCaseAndType(author, type);
        }

        return ResponseEntity.ok(
                ApiResponse.builder().success(true).data(books).build()
        );
    }

    // Get all books (for Master List report)
    @GetMapping
    public ResponseEntity<?> getAllBooks(@RequestParam(defaultValue = "BOOK") String type) {
        List<Book> books = bookRepository.findByType(type);
        return ResponseEntity.ok(ApiResponse.builder().success(true).data(books).build());
    }

    // Get single book by serial number
    @GetMapping("/{serialNo}")
    public ResponseEntity<?> getBookBySerialNo(@PathVariable String serialNo) {
        return bookRepository.findBySerialNo(serialNo)
                .map(book -> ResponseEntity.ok(ApiResponse.builder().success(true).data(book).build()))
                .orElse(ResponseEntity.notFound().build());
    }

    // Add book/movie (Admin only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        if (book.getName() == null || book.getName().isBlank()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().success(false).message("Book name is required").build()
            );
        }

        // Auto-generate serial number
        String prefix = generateSerialPrefix(book.getCategory(), book.getType());
        long count = bookRepository.countBySerialNoStartingWith(prefix);
        int copies = book.getTotalCopies() != null ? book.getTotalCopies() : 1;

        // Create one entry per copy
        for (int i = 0; i < copies; i++) {
            long serialNum = count + i + 1;
            String serialNo = String.format("%s%06d", prefix, serialNum);
            Book copy = Book.builder()
                    .serialNo(serialNo)
                    .name(book.getName())
                    .authorName(book.getAuthorName())
                    .category(book.getCategory())
                    .type(book.getType())
                    .status("AVAILABLE")
                    .cost(book.getCost())
                    .procurementDate(book.getProcurementDate() != null
                            ? book.getProcurementDate() : LocalDate.now())
                    .totalCopies(1)
                    .availableCopies(1)
                    .build();
            bookRepository.save(copy);
        }

        return ResponseEntity.ok(
                ApiResponse.builder().success(true)
                        .message(copies + " copy/copies of '" + book.getName() + "' added successfully").build()
        );
    }

    // Update book (Admin only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBook(@PathVariable String id, @RequestBody Book updated) {
        return bookRepository.findById(id).map(book -> {
            if (updated.getName() != null)        book.setName(updated.getName());
            if (updated.getStatus() != null)      book.setStatus(updated.getStatus());
            if (updated.getAuthorName() != null)  book.setAuthorName(updated.getAuthorName());
            if (updated.getCost() != null)        book.setCost(updated.getCost());
            if (updated.getProcurementDate() != null) book.setProcurementDate(updated.getProcurementDate());
            bookRepository.save(book);
            return ResponseEntity.ok(ApiResponse.builder().success(true)
                    .message("Book updated successfully").data(book).build());
        }).orElse(ResponseEntity.notFound().build());
    }

    private String generateSerialPrefix(String category, String type) {
        String catCode = switch (category != null ? category.toUpperCase() : "") {
            case "SCIENCE"              -> "SC";
            case "ECONOMICS"            -> "EC";
            case "FICTION"              -> "FC";
            case "CHILDREN"             -> "CH";
            case "PERSONAL DEVELOPMENT" -> "PD";
            default                     -> "GN";
        };
        String typeCode = "MOVIE".equalsIgnoreCase(type) ? "M" : "B";
        return catCode + "(" + typeCode + ")";
    }
}