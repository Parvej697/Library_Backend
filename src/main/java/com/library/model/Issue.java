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

    private LocalDate expectedReturnDate;

    private LocalDate actualReturnDate;

    private String status;

    private Double fineCalculated;

    private boolean finePaid;

    private String remarks;

    private String itemType;
}