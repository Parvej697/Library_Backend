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
@Document(collection = "books")
public class Book {

    @Id
    private String id;

    // SC(B)000001 for Science Book, SC(M)000001 for Science Movie, etc.
    private String serialNo;

    private String name;

    private String authorName;

    private String category; // Science, Economics, Fiction, Children, Personal Development

    // BOOK or MOVIE
    private String type;

    // AVAILABLE, ISSUED, LOST, DAMAGED
    private String status;

    private Double cost;

    private LocalDate procurementDate;

    private Integer totalCopies;

    private Integer availableCopies;
}