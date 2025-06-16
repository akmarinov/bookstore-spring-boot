package com.example.bookstore.repository;

import com.example.bookstore.model.Book;
import com.example.bookstore.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("BookRepository Tests")
class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    private Book book1;
    private Book book2;
    private Book book3;
    private Book outOfStockBook;

    @BeforeEach
    void setUp() {
        book1 = TestDataBuilder.aBook()
                .withTitle("The Great Gatsby")
                .withAuthor("F. Scott Fitzgerald")
                .withPrice("12.99")
                .withIsbn("978-0-7432-7356-5")
                .withCategory("Fiction")
                .withPublisher("Scribner")
                .withPublicationDate(LocalDate.of(1925, 4, 10))
                .withStockQuantity(10)
                .build();

        book2 = TestDataBuilder.aBook()
                .withTitle("To Kill a Mockingbird")
                .withAuthor("Harper Lee")
                .withPrice("14.99")
                .withIsbn("978-0-06-112008-4")
                .withCategory("Fiction")
                .withPublisher("J. B. Lippincott & Co.")
                .withPublicationDate(LocalDate.of(1960, 7, 11))
                .withStockQuantity(15)
                .build();

        book3 = TestDataBuilder.aBook()
                .withTitle("Clean Code")
                .withAuthor("Robert C. Martin")
                .withPrice("49.99")
                .withIsbn("978-0-13-235088-4")
                .withCategory("Technology")
                .withPublisher("Prentice Hall")
                .withPublicationDate(LocalDate.of(2008, 8, 1))
                .withStockQuantity(25)
                .build();

        outOfStockBook = TestDataBuilder.aBook()
                .withTitle("Out of Stock Book")
                .withAuthor("Test Author")
                .withPrice("19.99")
                .withIsbn("978-0-123-45678-9")
                .withCategory("Fiction")
                .withStockQuantity(0)
                .build();

        // Persist test data
        entityManager.persistAndFlush(book1);
        entityManager.persistAndFlush(book2);
        entityManager.persistAndFlush(book3);
        entityManager.persistAndFlush(outOfStockBook);
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Should save and find book by ID")
        void shouldSaveAndFindBookById() {
            // Given
            Book newBook = TestDataBuilder.aBook()
                    .withTitle("New Test Book")
                    .withIsbn("978-0-111-11111-1")
                    .build();

            // When
            Book savedBook = bookRepository.save(newBook);
            Optional<Book> foundBook = bookRepository.findById(savedBook.getId());

            // Then
            assertThat(foundBook).isPresent();
            assertThat(foundBook.get().getTitle()).isEqualTo("New Test Book");
            assertThat(foundBook.get().getIsbn()).isEqualTo("978-0-111-11111-1");
        }

        @Test
        @DisplayName("Should find all books with pagination")
        void shouldFindAllBooksWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 2);

            // When
            Page<Book> booksPage = bookRepository.findAll(pageable);

            // Then
            assertThat(booksPage.getContent()).hasSize(2);
            assertThat(booksPage.getTotalElements()).isEqualTo(4);
            assertThat(booksPage.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should delete book by ID")
        void shouldDeleteBookById() {
            // Given
            Long bookId = book1.getId();

            // When
            bookRepository.deleteById(bookId);
            Optional<Book> deletedBook = bookRepository.findById(bookId);

            // Then
            assertThat(deletedBook).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find by ISBN Tests")
    class FindByIsbnTests {

        @Test
        @DisplayName("Should find book by existing ISBN")
        void shouldFindBookByExistingIsbn() {
            // When
            Optional<Book> foundBook = bookRepository.findByIsbn("978-0-7432-7356-5");

            // Then
            assertThat(foundBook).isPresent();
            assertThat(foundBook.get().getTitle()).isEqualTo("The Great Gatsby");
            assertThat(foundBook.get().getAuthor()).isEqualTo("F. Scott Fitzgerald");
        }

        @Test
        @DisplayName("Should return empty optional for non-existing ISBN")
        void shouldReturnEmptyOptionalForNonExistingIsbn() {
            // When
            Optional<Book> foundBook = bookRepository.findByIsbn("978-0-000-00000-0");

            // Then
            assertThat(foundBook).isEmpty();
        }
    }

    @Nested
    @DisplayName("Search by Title Tests")
    class SearchByTitleTests {

        @Test
        @DisplayName("Should find books by title containing keyword (case insensitive)")
        void shouldFindBooksByTitleContainingKeywordCaseInsensitive() {
            // When
            Page<Book> books = bookRepository.findByTitleContainingIgnoreCase("great", PageRequest.of(0, 10));

            // Then
            assertThat(books.getContent()).hasSize(1);
            assertThat(books.getContent().get(0).getTitle()).isEqualTo("The Great Gatsby");
        }

        @Test
        @DisplayName("Should find books by partial title match")
        void shouldFindBooksByPartialTitleMatch() {
            // When
            Page<Book> books = bookRepository.findByTitleContainingIgnoreCase("kill", PageRequest.of(0, 10));

            // Then
            assertThat(books.getContent()).hasSize(1);
            assertThat(books.getContent().get(0).getTitle()).isEqualTo("To Kill a Mockingbird");
        }

        @Test
        @DisplayName("Should return empty page when no title matches")
        void shouldReturnEmptyPageWhenNoTitleMatches() {
            // When
            Page<Book> books = bookRepository.findByTitleContainingIgnoreCase("nonexistent", PageRequest.of(0, 10));

            // Then
            assertThat(books.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should find books by title without pagination")
        void shouldFindBooksByTitleWithoutPagination() {
            // When
            List<Book> books = bookRepository.findByTitleContainingIgnoreCase("The");

            // Then
            assertThat(books).hasSize(1);
            assertThat(books.get(0).getTitle()).isEqualTo("The Great Gatsby");
        }
    }

    @Nested
    @DisplayName("Search by Author Tests")
    class SearchByAuthorTests {

        @Test
        @DisplayName("Should find books by author containing keyword (case insensitive)")
        void shouldFindBooksByAuthorContainingKeywordCaseInsensitive() {
            // When
            Page<Book> books = bookRepository.findByAuthorContainingIgnoreCase("martin", PageRequest.of(0, 10));

            // Then
            assertThat(books.getContent()).hasSize(1);
            assertThat(books.getContent().get(0).getAuthor()).isEqualTo("Robert C. Martin");
        }

        @Test
        @DisplayName("Should find books by partial author match")
        void shouldFindBooksByPartialAuthorMatch() {
            // When
            Page<Book> books = bookRepository.findByAuthorContainingIgnoreCase("scott", PageRequest.of(0, 10));

            // Then
            assertThat(books.getContent()).hasSize(1);
            assertThat(books.getContent().get(0).getAuthor()).isEqualTo("F. Scott Fitzgerald");
        }

        @Test
        @DisplayName("Should find books by author without pagination")
        void shouldFindBooksByAuthorWithoutPagination() {
            // When
            List<Book> books = bookRepository.findByAuthorContainingIgnoreCase("Harper");

            // Then
            assertThat(books).hasSize(1);
            assertThat(books.get(0).getAuthor()).isEqualTo("Harper Lee");
        }
    }

    @Nested
    @DisplayName("Search by Category Tests")
    class SearchByCategoryTests {

        @Test
        @DisplayName("Should find books by category containing keyword (case insensitive)")
        void shouldFindBooksByCategoryContainingKeywordCaseInsensitive() {
            // When
            Page<Book> books = bookRepository.findByCategoryContainingIgnoreCase("fiction", PageRequest.of(0, 10));

            // Then
            assertThat(books.getContent()).hasSize(3); // book1, book2, outOfStockBook
            assertThat(books.getContent())
                    .extracting(Book::getCategory)
                    .containsOnly("Fiction");
        }

        @Test
        @DisplayName("Should find books by exact category match")
        void shouldFindBooksByExactCategoryMatch() {
            // When
            Page<Book> books = bookRepository.findByCategoryContainingIgnoreCase("Technology", PageRequest.of(0, 10));

            // Then
            assertThat(books.getContent()).hasSize(1);
            assertThat(books.getContent().get(0).getCategory()).isEqualTo("Technology");
        }

        @Test
        @DisplayName("Should find books by category without pagination")
        void shouldFindBooksByCategoryWithoutPagination() {
            // When
            List<Book> books = bookRepository.findByCategoryContainingIgnoreCase("Technology");

            // Then
            assertThat(books).hasSize(1);
            assertThat(books.get(0).getCategory()).isEqualTo("Technology");
        }
    }

    @Nested
    @DisplayName("Custom Query Tests")
    class CustomQueryTests {

        @Test
        @DisplayName("Should find books by title and author with both parameters")
        void shouldFindBooksByTitleAndAuthorWithBothParameters() {
            // When
            Page<Book> books = bookRepository.findByTitleAndAuthor("Great", "Fitzgerald", PageRequest.of(0, 10));

            // Then
            assertThat(books.getContent()).hasSize(1);
            assertThat(books.getContent().get(0).getTitle()).isEqualTo("The Great Gatsby");
            assertThat(books.getContent().get(0).getAuthor()).isEqualTo("F. Scott Fitzgerald");
        }

        @Test
        @DisplayName("Should find books by title only when author is null")
        void shouldFindBooksByTitleOnlyWhenAuthorIsNull() {
            // When
            Page<Book> books = bookRepository.findByTitleAndAuthor("Great", null, PageRequest.of(0, 10));

            // Then
            assertThat(books.getContent()).hasSize(1);
            assertThat(books.getContent().get(0).getTitle()).isEqualTo("The Great Gatsby");
        }

        @Test
        @DisplayName("Should find books by author only when title is null")
        void shouldFindBooksByAuthorOnlyWhenTitleIsNull() {
            // When
            Page<Book> books = bookRepository.findByTitleAndAuthor(null, "Martin", PageRequest.of(0, 10));

            // Then
            assertThat(books.getContent()).hasSize(1);
            assertThat(books.getContent().get(0).getAuthor()).isEqualTo("Robert C. Martin");
        }

        @Test
        @DisplayName("Should return all books when both parameters are null")
        void shouldReturnAllBooksWhenBothParametersAreNull() {
            // When
            Page<Book> books = bookRepository.findByTitleAndAuthor(null, null, PageRequest.of(0, 10));

            // Then
            assertThat(books.getContent()).hasSize(4);
        }

        @Test
        @DisplayName("Should search books by title or author")
        void shouldSearchBooksByTitleOrAuthor() {
            // When
            List<Book> books = bookRepository.searchByTitleOrAuthor("Great");

            // Then
            assertThat(books).hasSize(1);
            assertThat(books.get(0).getTitle()).isEqualTo("The Great Gatsby");
        }

        @Test
        @DisplayName("Should search books by title or author with pagination")
        void shouldSearchBooksByTitleOrAuthorWithPagination() {
            // When
            Page<Book> books = bookRepository.searchByTitleOrAuthorWithPagination("a", PageRequest.of(0, 10));

            // Then
            assertThat(books.getContent()).hasSizeGreaterThan(0);
            // Should find books with 'a' in title or author
        }
    }

    @Nested
    @DisplayName("Stock Quantity Tests")
    class StockQuantityTests {

        @Test
        @DisplayName("Should find books with stock quantity greater than zero")
        void shouldFindBooksWithStockQuantityGreaterThanZero() {
            // When
            Page<Book> books = bookRepository.findByStockQuantityGreaterThan(0, PageRequest.of(0, 10));

            // Then
            assertThat(books.getContent()).hasSize(3); // book1, book2, book3 (outOfStockBook has 0 stock)
            assertThat(books.getContent())
                    .extracting(Book::getStockQuantity)
                    .allMatch(quantity -> quantity > 0);
        }

        @Test
        @DisplayName("Should find books with stock quantity greater than specific value")
        void shouldFindBooksWithStockQuantityGreaterThanSpecificValue() {
            // When
            Page<Book> books = bookRepository.findByStockQuantityGreaterThan(15, PageRequest.of(0, 10));

            // Then
            assertThat(books.getContent()).hasSize(1); // Only book3 has stock > 15
            assertThat(books.getContent().get(0).getTitle()).isEqualTo("Clean Code");
            assertThat(books.getContent().get(0).getStockQuantity()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("Existence and Count Tests")
    class ExistenceAndCountTests {

        @Test
        @DisplayName("Should check if book exists by title and author (case insensitive)")
        void shouldCheckIfBookExistsByTitleAndAuthorCaseInsensitive() {
            // When
            boolean exists = bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase("the great gatsby", "f. scott fitzgerald");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when book does not exist by title and author")
        void shouldReturnFalseWhenBookDoesNotExistByTitleAndAuthor() {
            // When
            boolean exists = bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase("Non-existent Book", "Non-existent Author");

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should check if book exists by title and author excluding specific ID")
        void shouldCheckIfBookExistsByTitleAndAuthorExcludingSpecificId() {
            // When
            boolean exists = bookRepository.existsByTitleAndAuthorExcludingId("The Great Gatsby", "F. Scott Fitzgerald", 999L);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when checking existence excluding the same book ID")
        void shouldReturnFalseWhenCheckingExistenceExcludingTheSameBookId() {
            // When
            boolean exists = bookRepository.existsByTitleAndAuthorExcludingId("The Great Gatsby", "F. Scott Fitzgerald", book1.getId());

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should count books by category")
        void shouldCountBooksByCategory() {
            // When
            long count = bookRepository.countByCategory("Fiction");

            // Then
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return zero count for non-existent category")
        void shouldReturnZeroCountForNonExistentCategory() {
            // When
            long count = bookRepository.countByCategory("Non-existent Category");

            // Then
            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should handle pagination correctly")
        void shouldHandlePaginationCorrectly() {
            // Given
            Pageable firstPage = PageRequest.of(0, 2);
            Pageable secondPage = PageRequest.of(1, 2);

            // When
            Page<Book> firstPageResult = bookRepository.findAll(firstPage);
            Page<Book> secondPageResult = bookRepository.findAll(secondPage);

            // Then
            assertThat(firstPageResult.getContent()).hasSize(2);
            assertThat(secondPageResult.getContent()).hasSize(2);
            assertThat(firstPageResult.getTotalElements()).isEqualTo(4);
            assertThat(secondPageResult.getTotalElements()).isEqualTo(4);
            assertThat(firstPageResult.getNumber()).isEqualTo(0);
            assertThat(secondPageResult.getNumber()).isEqualTo(1);

            // Ensure no overlap between pages
            assertThat(firstPageResult.getContent())
                    .extracting(Book::getId)
                    .doesNotContainAnyElementsOf(
                            secondPageResult.getContent().stream()
                                    .map(Book::getId)
                                    .toList()
                    );
        }

        @Test
        @DisplayName("Should handle search with pagination")
        void shouldHandleSearchWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 1);

            // When
            Page<Book> result = bookRepository.findByCategoryContainingIgnoreCase("Fiction", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.hasNext()).isTrue();
        }
    }
}