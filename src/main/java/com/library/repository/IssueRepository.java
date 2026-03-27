package com.library.repository;

import com.library.model.Issue;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IssueRepository extends MongoRepository<Issue, String> {
    List<Issue> findByStatus(String status);
    List<Issue> findByMembershipId(String membershipId);
    Optional<Issue> findByBookSerialNoAndStatus(String serialNo, String status);
    List<Issue> findByExpectedReturnDateBeforeAndStatus(LocalDate date, String status);
    List<Issue> findByMembershipIdAndStatus(String membershipId, String status);
}