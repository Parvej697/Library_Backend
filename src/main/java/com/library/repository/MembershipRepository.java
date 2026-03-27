package com.library.repository;

import com.library.model.Membership;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends MongoRepository<Membership, String> {
    Optional<Membership> findByMembershipId(String membershipId);
    List<Membership> findByStatus(String status);
    long count();
}