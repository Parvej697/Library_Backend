package com.library.config;

import com.library.model.Book;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedBooks();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            // Admin user
            userRepository.save(User.builder()
                    .username("adm")
                    .password(passwordEncoder.encode("adm"))
                    .name("Administrator")
                    .isAdmin(true)
                    .isActive(true)
                    .build());

            // Regular user
            userRepository.save(User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user"))
                    .name("Library User")
                    .isAdmin(false)
                    .isActive(true)
                    .build());

            log.info("Seeded default users: adm/adm and user/user");
        }
    }

    private void seedBooks() {
        if (bookRepository.count() == 0) {
            List<Book> books = List.of(
                    Book.builder().serialNo("SC(B)000001").name("A Brief History of Time")
                            .authorName("Stephen Hawking").category("Science").type("BOOK")
                            .status("AVAILABLE").cost(450.0).procurementDate(LocalDate.of(2022, 1, 15))
                            .totalCopies(1).availableCopies(1).build(),
                    Book.builder().serialNo("SC(B)000002").name("The Selfish Gene")
                            .authorName("Richard Dawkins").category("Science").type("BOOK")
                            .status("AVAILABLE").cost(380.0).procurementDate(LocalDate.of(2022, 3, 10))
                            .totalCopies(1).availableCopies(1).build(),
                    Book.builder().serialNo("SC(B)000003").name("Cosmos")
                            .authorName("Carl Sagan").category("Science").type("BOOK")
                            .status("AVAILABLE").cost(520.0).procurementDate(LocalDate.of(2022, 6, 5))
                            .totalCopies(1).availableCopies(1).build(),
                    Book.builder().serialNo("SC(B)000004").name("Sapiens")
                            .authorName("Yuval Noah Harari").category("Science").type("BOOK")
                            .status("AVAILABLE").cost(499.0).procurementDate(LocalDate.of(2023, 1, 20))
                            .totalCopies(1).availableCopies(1).build(),
                    Book.builder().serialNo("EC(B)000001").name("The Wealth of Nations")
                            .authorName("Adam Smith").category("Economics").type("BOOK")
                            .status("AVAILABLE").cost(699.0).procurementDate(LocalDate.of(2021, 8, 12))
                            .totalCopies(1).availableCopies(1).build(),
                    Book.builder().serialNo("EC(B)000002").name("Freakonomics")
                            .authorName("Steven Levitt").category("Economics").type("BOOK")
                            .status("AVAILABLE").cost(350.0).procurementDate(LocalDate.of(2022, 4, 18))
                            .totalCopies(1).availableCopies(1).build(),
                    Book.builder().serialNo("FC(B)000001").name("To Kill a Mockingbird")
                            .authorName("Harper Lee").category("Fiction").type("BOOK")
                            .status("AVAILABLE").cost(299.0).procurementDate(LocalDate.of(2021, 11, 3))
                            .totalCopies(1).availableCopies(1).build(),
                    Book.builder().serialNo("FC(B)000002").name("1984")
                            .authorName("George Orwell").category("Fiction").type("BOOK")
                            .status("AVAILABLE").cost(275.0).procurementDate(LocalDate.of(2022, 2, 28))
                            .totalCopies(1).availableCopies(1).build(),
                    Book.builder().serialNo("CH(B)000001").name("Harry Potter and the Philosopher's Stone")
                            .authorName("J.K. Rowling").category("Children").type("BOOK")
                            .status("AVAILABLE").cost(399.0).procurementDate(LocalDate.of(2022, 7, 14))
                            .totalCopies(1).availableCopies(1).build(),
                    Book.builder().serialNo("CH(B)000002").name("The Lion the Witch and the Wardrobe")
                            .authorName("C.S. Lewis").category("Children").type("BOOK")
                            .status("AVAILABLE").cost(325.0).procurementDate(LocalDate.of(2023, 3, 9))
                            .totalCopies(1).availableCopies(1).build(),
                    Book.builder().serialNo("PD(B)000001").name("Atomic Habits")
                            .authorName("James Clear").category("Personal Development").type("BOOK")
                            .status("AVAILABLE").cost(549.0).procurementDate(LocalDate.of(2023, 5, 1))
                            .totalCopies(1).availableCopies(1).build(),
                    Book.builder().serialNo("PD(B)000002").name("Think and Grow Rich")
                            .authorName("Napoleon Hill").category("Personal Development").type("BOOK")
                            .status("AVAILABLE").cost(199.0).procurementDate(LocalDate.of(2022, 9, 22))
                            .totalCopies(1).availableCopies(1).build(),
                    // Sample Movies
                    Book.builder().serialNo("SC(M)000001").name("Interstellar")
                            .authorName("Christopher Nolan").category("Science").type("MOVIE")
                            .status("AVAILABLE").cost(250.0).procurementDate(LocalDate.of(2023, 1, 5))
                            .totalCopies(1).availableCopies(1).build(),
                    Book.builder().serialNo("FC(M)000001").name("The Shawshank Redemption")
                            .authorName("Frank Darabont").category("Fiction").type("MOVIE")
                            .status("AVAILABLE").cost(200.0).procurementDate(LocalDate.of(2022, 12, 10))
                            .totalCopies(1).availableCopies(1).build()
            );

            bookRepository.saveAll(books);
            log.info("Seeded {} sample books and movies", books.size());
        }
    }
}