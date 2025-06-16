package com.example.bookstore.performance;

import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.service.BookService;
import com.example.bookstore.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("BookService Performance Tests")
class BookServicePerformanceTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    @Test
    @DisplayName("Should handle large dataset creation efficiently")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldHandleLargeDatasetCreationEfficiently() {
        // Given - Create a large number of books
        int numberOfBooks = 1000;
        List<Book> books = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // When - Create books in batches
        for (int i = 1; i <= numberOfBooks; i++) {
            Book book = TestDataBuilder.aBook()
                    .withTitle("Performance Test Book " + i)
                    .withAuthor("Performance Author " + (i % 100)) // 100 different authors
                    .withIsbn("978-0-perf-" + String.format("%06d", i))
                    .withCategory("Category " + (i % 10)) // 10 different categories
                    .withStockQuantity(i % 50) // Varying stock quantities
                    .build();
            
            Book savedBook = bookService.createBook(book);
            books.add(savedBook);

            // Log progress every 100 books
            if (i % 100 == 0) {
                System.out.println("Created " + i + " books");
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then - Verify all books were created and performance is acceptable
        assertThat(books).hasSize(numberOfBooks);
        assertThat(duration).isLessThan(30000); // Should complete within 30 seconds
        
        System.out.println("Created " + numberOfBooks + " books in " + duration + "ms");
        System.out.println("Average time per book: " + (duration / (double) numberOfBooks) + "ms");
    }

    @Test
    @DisplayName("Should handle large dataset pagination efficiently")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void shouldHandleLargeDatasetPaginationEfficiently() {
        // Given - Create a moderate dataset
        int numberOfBooks = 500;
        for (int i = 1; i <= numberOfBooks; i++) {
            Book book = TestDataBuilder.aBook()
                    .withTitle("Pagination Test Book " + i)
                    .withAuthor("Author " + (i % 20))
                    .withIsbn("978-0-page-" + String.format("%06d", i))
                    .withCategory("Category " + (i % 5))
                    .build();
            bookService.createBook(book);
        }

        // When - Test pagination performance
        long startTime = System.currentTimeMillis();
        
        int pageSize = 20;
        int totalPages = numberOfBooks / pageSize;
        
        for (int page = 0; page < totalPages; page++) {
            Page<Book> bookPage = bookService.getAllBooks(PageRequest.of(page, pageSize));
            assertThat(bookPage.getContent()).hasSize(pageSize);
            assertThat(bookPage.getTotalElements()).isEqualTo(numberOfBooks);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then - Verify pagination performance
        assertThat(duration).isLessThan(10000); // Should complete within 10 seconds
        
        System.out.println("Paginated through " + numberOfBooks + " books in " + duration + "ms");
        System.out.println("Average time per page: " + (duration / (double) totalPages) + "ms");
    }

    @Test
    @DisplayName("Should handle concurrent search operations efficiently")
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void shouldHandleConcurrentSearchOperationsEfficiently() {
        // Given - Create test data
        int numberOfBooks = 200;
        for (int i = 1; i <= numberOfBooks; i++) {
            Book book = TestDataBuilder.aBook()
                    .withTitle("Search Test Book " + i)
                    .withAuthor("Search Author " + (i % 10))
                    .withIsbn("978-0-search-" + String.format("%06d", i))
                    .withCategory("SearchCategory" + (i % 5))
                    .build();
            bookService.createBook(book);
        }

        // When - Perform multiple search operations
        long startTime = System.currentTimeMillis();
        
        // Test various search scenarios
        for (int i = 0; i < 50; i++) {
            // Search by title
            Page<Book> titleResults = bookService.searchBooksByTitle("Book", PageRequest.of(0, 10));
            assertThat(titleResults.getContent()).isNotEmpty();

            // Search by author
            Page<Book> authorResults = bookService.searchBooksByAuthor("Author", PageRequest.of(0, 10));
            assertThat(authorResults.getContent()).isNotEmpty();

            // Search by category
            Page<Book> categoryResults = bookService.searchBooksByCategory("SearchCategory", PageRequest.of(0, 10));
            assertThat(categoryResults.getContent()).isNotEmpty();

            // Combined search
            Page<Book> combinedResults = bookService.searchBooks("Book", "Author", PageRequest.of(0, 10));
            assertThat(combinedResults.getContent()).isNotEmpty();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then - Verify search performance
        assertThat(duration).isLessThan(15000); // Should complete within 15 seconds
        
        System.out.println("Performed 200 search operations in " + duration + "ms");
        System.out.println("Average time per search: " + (duration / 200.0) + "ms");
    }

    @Test
    @DisplayName("Should handle bulk updates efficiently")
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void shouldHandleBulkUpdatesEfficiently() {
        // Given - Create initial dataset
        int numberOfBooks = 300;
        List<Long> bookIds = new ArrayList<>();
        
        for (int i = 1; i <= numberOfBooks; i++) {
            Book book = TestDataBuilder.aBook()
                    .withTitle("Update Test Book " + i)
                    .withAuthor("Update Author " + i)
                    .withIsbn("978-0-update-" + String.format("%06d", i))
                    .withStockQuantity(10)
                    .build();
            Book savedBook = bookService.createBook(book);
            bookIds.add(savedBook.getId());
        }

        // When - Perform bulk updates
        long startTime = System.currentTimeMillis();
        
        for (Long bookId : bookIds) {
            Book existingBook = bookService.getBookById(bookId).orElse(null);
            if (existingBook != null) {
                existingBook.setTitle("Updated Book " + bookId);
                existingBook.setAuthor("Updated Author " + bookId);
                existingBook.setStockQuantity(20);
                bookService.updateBook(bookId, existingBook);
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then - Verify update performance and correctness
        assertThat(duration).isLessThan(20000); // Should complete within 20 seconds
        
        // Verify a few random updates
        Book randomBook = bookService.getBookById(bookIds.get(50)).orElse(null);
        assertThat(randomBook).isNotNull();
        assertThat(randomBook.getTitle()).startsWith("Updated Book");
        assertThat(randomBook.getStockQuantity()).isEqualTo(20);
        
        System.out.println("Updated " + numberOfBooks + " books in " + duration + "ms");
        System.out.println("Average time per update: " + (duration / (double) numberOfBooks) + "ms");
    }

    @Test
    @DisplayName("Should handle mixed CRUD operations efficiently")
    @Timeout(value = 25, unit = TimeUnit.SECONDS)
    void shouldHandleMixedCrudOperationsEfficiently() {
        // Given - Initial setup
        int operationsCount = 500;
        List<Long> bookIds = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // When - Perform mixed operations
        for (int i = 1; i <= operationsCount; i++) {
            if (i % 4 == 1) {
                // CREATE operation
                Book book = TestDataBuilder.aBook()
                        .withTitle("Mixed Test Book " + i)
                        .withAuthor("Mixed Author " + i)
                        .withIsbn("978-0-mixed-" + String.format("%06d", i))
                        .build();
                Book savedBook = bookService.createBook(book);
                bookIds.add(savedBook.getId());
                
            } else if (i % 4 == 2 && !bookIds.isEmpty()) {
                // READ operation
                Long randomId = bookIds.get((int) (Math.random() * bookIds.size()));
                bookService.getBookById(randomId);
                
            } else if (i % 4 == 3 && !bookIds.isEmpty()) {
                // UPDATE operation
                Long randomId = bookIds.get((int) (Math.random() * bookIds.size()));
                Book existingBook = bookService.getBookById(randomId).orElse(null);
                if (existingBook != null) {
                    existingBook.setTitle("Updated Mixed Book " + i);
                    existingBook.setStockQuantity(25);
                    bookService.updateBook(randomId, existingBook);
                }
                
            } else if (i % 4 == 0 && !bookIds.isEmpty()) {
                // DELETE operation (occasionally)
                if (bookIds.size() > 10 && Math.random() < 0.1) {
                    Long randomId = bookIds.remove((int) (Math.random() * bookIds.size()));
                    bookService.deleteBook(randomId);
                }
            }

            // Occasional search operations
            if (i % 10 == 0) {
                bookService.searchBooksByTitle("Mixed", PageRequest.of(0, 5));
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then - Verify mixed operations performance
        assertThat(duration).isLessThan(25000); // Should complete within 25 seconds
        assertThat(bookIds).isNotEmpty(); // Should have some books remaining
        
        System.out.println("Performed " + operationsCount + " mixed CRUD operations in " + duration + "ms");
        System.out.println("Average time per operation: " + (duration / (double) operationsCount) + "ms");
        System.out.println("Remaining books: " + bookIds.size());
    }

    @Test
    @DisplayName("Should handle memory efficiently with large result sets")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void shouldHandleMemoryEfficientlyWithLargeResultSets() {
        // Given - Create a dataset
        int numberOfBooks = 200;
        for (int i = 1; i <= numberOfBooks; i++) {
            Book book = TestDataBuilder.aBook()
                    .withTitle("Memory Test Book " + i)
                    .withAuthor("Memory Author")
                    .withCategory("Memory")
                    .build();
            bookService.createBook(book);
        }

        // When - Test pagination vs loading all at once
        long startTime = System.currentTimeMillis();
        
        // Use pagination to process all books (memory efficient)
        int pageSize = 50;
        int page = 0;
        int totalProcessed = 0;
        
        Page<Book> currentPage;
        do {
            currentPage = bookService.getAllBooks(PageRequest.of(page, pageSize));
            totalProcessed += currentPage.getContent().size();
            
            // Simulate some processing
            for (Book book : currentPage.getContent()) {
                assertThat(book.getTitle()).startsWith("Memory Test Book");
            }
            
            page++;
        } while (currentPage.hasNext());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then - Verify memory efficient processing
        assertThat(totalProcessed).isEqualTo(numberOfBooks);
        assertThat(duration).isLessThan(10000); // Should complete within 10 seconds
        
        System.out.println("Processed " + totalProcessed + " books using pagination in " + duration + "ms");
    }
}