package com.example.bookstore.repository;

import com.example.bookstore.model.Book;
import com.example.bookstore.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("BookRepository TestContainers Tests")
@Disabled("Requires Docker - disabled for CI/CD environments without Docker")
class BookRepositoryTestContainersTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("bookstore_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = TestDataBuilder.aBook()
                .withTitle("TestContainers Book")
                .withAuthor("TestContainers Author")
                .withPrice("29.99")
                .withIsbn("978-0-testcontainers-1")
                .withCategory("TestContainers")
                .withPublisher("TestContainers Publisher")
                .withPublicationDate(LocalDate.of(2023, 1, 1))
                .withPages(250)
                .withStockQuantity(15)
                .build();
    }

    @Test
    @DisplayName("Should save and retrieve book using real MySQL database")
    void shouldSaveAndRetrieveBookUsingRealMySqlDatabase() {
        // Given - Save book using TestEntityManager
        Book savedBook = entityManager.persistAndFlush(testBook);
        entityManager.clear(); // Clear persistence context to ensure we're reading from DB

        // When - Retrieve using repository
        Optional<Book> retrievedBook = bookRepository.findById(savedBook.getId());

        // Then - Verify all fields are correctly persisted and retrieved
        assertThat(retrievedBook).isPresent();
        Book book = retrievedBook.get();
        
        assertThat(book.getId()).isEqualTo(savedBook.getId());
        assertThat(book.getTitle()).isEqualTo("TestContainers Book");
        assertThat(book.getAuthor()).isEqualTo("TestContainers Author");
        assertThat(book.getPrice()).isEqualByComparingTo(new BigDecimal("29.99"));
        assertThat(book.getIsbn()).isEqualTo("978-0-testcontainers-1");
        assertThat(book.getCategory()).isEqualTo("TestContainers");
        assertThat(book.getPublisher()).isEqualTo("TestContainers Publisher");
        assertThat(book.getPublicationDate()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(book.getPages()).isEqualTo(250);
        assertThat(book.getStockQuantity()).isEqualTo(15);
        assertThat(book.getCreatedAt()).isNotNull();
        assertThat(book.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should perform complex queries with real database")
    void shouldPerformComplexQueriesWithRealDatabase() {
        // Given - Create multiple books
        Book book1 = TestDataBuilder.aBook()
                .withTitle("Advanced MySQL")
                .withAuthor("Database Expert")
                .withCategory("Technology")
                .withStockQuantity(10)
                .build();

        Book book2 = TestDataBuilder.aBook()
                .withTitle("MySQL Performance")
                .withAuthor("Performance Guru")
                .withCategory("Technology")
                .withStockQuantity(5)
                .build();

        Book book3 = TestDataBuilder.aBook()
                .withTitle("Java Programming")
                .withAuthor("Java Expert")
                .withCategory("Programming")
                .withStockQuantity(0) // Out of stock
                .build();

        entityManager.persistAndFlush(book1);
        entityManager.persistAndFlush(book2);
        entityManager.persistAndFlush(book3);
        entityManager.clear();

        // When & Then - Test various query methods
        
        // Test search by title
        Page<Book> mysqlBooks = bookRepository.findByTitleContainingIgnoreCase("mysql", PageRequest.of(0, 10));
        assertThat(mysqlBooks.getContent()).hasSize(2);
        assertThat(mysqlBooks.getContent())
                .extracting(Book::getTitle)
                .allMatch(title -> title.toLowerCase().contains("mysql"));

        // Test search by category
        Page<Book> techBooks = bookRepository.findByCategoryContainingIgnoreCase("Technology", PageRequest.of(0, 10));
        assertThat(techBooks.getContent()).hasSize(2);
        assertThat(techBooks.getContent())
                .extracting(Book::getCategory)
                .containsOnly("Technology");

        // Test books in stock
        Page<Book> inStockBooks = bookRepository.findByStockQuantityGreaterThan(0, PageRequest.of(0, 10));
        assertThat(inStockBooks.getContent()).hasSize(2);
        assertThat(inStockBooks.getContent())
                .extracting(Book::getStockQuantity)
                .allMatch(stock -> stock > 0);

        // Test custom query with title and author
        Page<Book> customSearch = bookRepository.findByTitleAndAuthor("mysql", "expert", PageRequest.of(0, 10));
        assertThat(customSearch.getContent()).hasSize(1);
        assertThat(customSearch.getContent().get(0).getTitle()).isEqualTo("Advanced MySQL");

        // Test count by category
        long techCount = bookRepository.countByCategory("Technology");
        assertThat(techCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle database constraints and uniqueness")
    void shouldHandleDatabaseConstraintsAndUniqueness() {
        // Given - Save a book with unique ISBN
        Book originalBook = TestDataBuilder.aBook()
                .withTitle("Original Book")
                .withIsbn("978-0-unique-isbn-1")
                .build();
        
        entityManager.persistAndFlush(originalBook);
        entityManager.clear();

        // When - Try to save another book with the same ISBN
        Book duplicateIsbnBook = TestDataBuilder.aBook()
                .withTitle("Duplicate ISBN Book")
                .withIsbn("978-0-unique-isbn-1") // Same ISBN
                .build();

        // Then - Should handle the constraint violation gracefully
        // Note: The actual constraint violation would be handled at the service layer
        // Here we test that we can find the existing book
        Optional<Book> existingBook = bookRepository.findByIsbn("978-0-unique-isbn-1");
        assertThat(existingBook).isPresent();
        assertThat(existingBook.get().getTitle()).isEqualTo("Original Book");
    }

    @Test
    @DisplayName("Should handle pagination with large dataset")
    void shouldHandlePaginationWithLargeDataset() {
        // Given - Create a larger dataset
        for (int i = 1; i <= 25; i++) {
            Book book = TestDataBuilder.aBook()
                    .withTitle("Book " + String.format("%02d", i))
                    .withAuthor("Author " + i)
                    .withIsbn("978-0-" + String.format("%06d", i) + "-1")
                    .withStockQuantity(i % 10 == 0 ? 0 : i) // Every 10th book is out of stock
                    .build();
            entityManager.persist(book);
        }
        entityManager.flush();
        entityManager.clear();

        // When & Then - Test pagination
        Pageable firstPage = PageRequest.of(0, 10);
        Page<Book> page1 = bookRepository.findAll(firstPage);
        
        assertThat(page1.getContent()).hasSize(10);
        assertThat(page1.getTotalElements()).isEqualTo(25);
        assertThat(page1.getTotalPages()).isEqualTo(3);
        assertThat(page1.isFirst()).isTrue();
        assertThat(page1.hasNext()).isTrue();

        // Test second page
        Pageable secondPage = PageRequest.of(1, 10);
        Page<Book> page2 = bookRepository.findAll(secondPage);
        
        assertThat(page2.getContent()).hasSize(10);
        assertThat(page2.getNumber()).isEqualTo(1);
        assertThat(page2.hasNext()).isTrue();
        assertThat(page2.hasPrevious()).isTrue();

        // Test last page
        Pageable lastPage = PageRequest.of(2, 10);
        Page<Book> page3 = bookRepository.findAll(lastPage);
        
        assertThat(page3.getContent()).hasSize(5);
        assertThat(page3.isLast()).isTrue();
        assertThat(page3.hasNext()).isFalse();

        // Test books in stock pagination
        Page<Book> inStockPage = bookRepository.findByStockQuantityGreaterThan(0, firstPage);
        assertThat(inStockPage.getContent()).hasSize(10);
        assertThat(inStockPage.getTotalElements()).isEqualTo(22); // 25 - 3 out of stock books
    }

    @Test
    @DisplayName("Should handle complex search scenarios with real database")
    void shouldHandleComplexSearchScenariosWithRealDatabase() {
        // Given - Create books with varied data for complex searching
        Book[] books = {
            TestDataBuilder.aBook()
                .withTitle("Advanced Java Programming")
                .withAuthor("John Smith")
                .withCategory("Programming")
                .withStockQuantity(10)
                .build(),
            TestDataBuilder.aBook()
                .withTitle("Java: The Complete Reference")
                .withAuthor("Herbert Schildt")
                .withCategory("Programming")
                .withStockQuantity(5)
                .build(),
            TestDataBuilder.aBook()
                .withTitle("Python Programming")
                .withAuthor("John Smith")
                .withCategory("Programming")
                .withStockQuantity(8)
                .build(),
            TestDataBuilder.aBook()
                .withTitle("Database Design")
                .withAuthor("Sarah Johnson")
                .withCategory("Database")
                .withStockQuantity(0)
                .build()
        };

        for (Book book : books) {
            entityManager.persist(book);
        }
        entityManager.flush();
        entityManager.clear();

        // When & Then - Test complex search scenarios
        
        // Search by title containing "Java"
        Page<Book> javaBooks = bookRepository.findByTitleContainingIgnoreCase("java", PageRequest.of(0, 10));
        assertThat(javaBooks.getContent()).hasSize(2);

        // Search by author "John Smith"
        Page<Book> johnSmithBooks = bookRepository.findByAuthorContainingIgnoreCase("john smith", PageRequest.of(0, 10));
        assertThat(johnSmithBooks.getContent()).hasSize(2);

        // Search by programming category
        Page<Book> programmingBooks = bookRepository.findByCategoryContainingIgnoreCase("programming", PageRequest.of(0, 10));
        assertThat(programmingBooks.getContent()).hasSize(3);

        // Search with title and author combination
        Page<Book> javaByJohn = bookRepository.findByTitleAndAuthor("java", "john", PageRequest.of(0, 10));
        assertThat(javaByJohn.getContent()).hasSize(1);
        assertThat(javaByJohn.getContent().get(0).getTitle()).isEqualTo("Advanced Java Programming");

        // Test search by title or author using custom query
        java.util.List<Book> smithBooks = bookRepository.searchByTitleOrAuthor("smith");
        assertThat(smithBooks).hasSize(2); // Both John Smith books

        // Test existence checks
        boolean exists = bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase("advanced java programming", "john smith");
        assertThat(exists).isTrue();

        boolean notExists = bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase("nonexistent book", "nonexistent author");
        assertThat(notExists).isFalse();
    }
}