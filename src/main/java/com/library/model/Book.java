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

    private String serialNo;

    private String name;

    private String authorName;

    private String category;


    private String type;

    private String status;

    private Double cost;

    private LocalDate procurementDate;

    private Integer totalCopies;

    private Integer availableCopies;
}