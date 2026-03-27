package com.library.repository;

import com.library.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends MongoRepository<Book, String> {
    List<Book> findByNameContainingIgnoreCaseAndType(String name, String type);
    List<Book> findByAuthorNameContainingIgnoreCaseAndType(String authorName, String type);
    List<Book> findByNameContainingIgnoreCase(String name);
    List<Book> findByAuthorNameContainingIgnoreCase(String authorName);
    List<Book> findByType(String type);
    Optional<Book> findBySerialNo(String serialNo);
    List<Book> findByTypeAndStatus(String type, String status);
    long countBySerialNoStartingWith(String prefix);
}