package com.library.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "issues")
public class Issue {

    @Id
    private String id;

    private String bookSerialNo;

    private String bookName;

    private String authorName;

    private String membershipId;

    private LocalDate issueDate;

    private LocalDate expectedReturnDate; // issueDate + 15 days

    private LocalDate actualReturnDate;

    // ISSUED, RETURNED, OVERDUE
    private String status;

    private Double fineCalculated;

    private boolean finePaid;

    private String remarks;

    // BOOK or MOVIE
    private String itemType;
}