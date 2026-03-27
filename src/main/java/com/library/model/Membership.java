package com.library.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "memberships")
public class Membership {

    @Id
    private String id;

    @Indexed(unique = true)
    private String membershipId; // Auto-generated: MEM00001

    private String firstName;

    private String passwordHash;

    private String lastName;

    private String contactNumber;

    private String contactAddress;

    private String aadharCardNo;

    private LocalDate startDate;

    private LocalDate endDate;

    // ACTIVE, INACTIVE, CANCELLED
    private String status;

    private Double pendingFine;

    // SIX_MONTHS, ONE_YEAR, TWO_YEARS
    private String membershipType;
}