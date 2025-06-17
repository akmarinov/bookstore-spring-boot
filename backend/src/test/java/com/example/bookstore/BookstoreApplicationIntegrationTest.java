package com.example.bookstore;

import com.example.bookstore.dto.BookCreateRequest;
import com.example.bookstore.dto.BookUpdateRequest;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.util.TestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Bookstore Application Integration Tests")
class BookstoreApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @Nested
    @DisplayName("Full CRUD Integration Tests")
    class FullCrudIntegrationTests {
        
        @BeforeEach
        void setUp() {
            bookRepository.deleteAll();
        }

        @Test
        @DisplayName("Should perform complete CRUD operations successfully")
        void shouldPerformCompleteCrudOperationsSuccessfully() throws Exception {
            // 1. CREATE - Create a new book
            BookCreateRequest createRequest = TestDataBuilder.aBookCreateRequest()
                    .withTitle("Integration Test Book")
                    .withAuthor("Integration Author")
                    .withPrice("25.99")
                    .withIsbn("978-0-111-11111-1")
                    .withCategory("Integration")
                    .withPages(300)
                    .withStockQuantity(15)
                    .build();

            String createResponse = mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title", is("Integration Test Book")))
                    .andExpect(jsonPath("$.author", is("Integration Author")))
                    .andExpect(jsonPath("$.price", is(25.99)))
                    .andExpect(jsonPath("$.isbn", is("978-0-111-11111-1")))
                    .andExpect(jsonPath("$.id").exists())
                    .andReturn().getResponse().getContentAsString();

            Book createdBook = objectMapper.readValue(createResponse, Book.class);
            Long bookId = createdBook.getId();

            // Verify book is in database
            assertThat(bookRepository.findById(bookId)).isPresent();

            // 2. READ - Get the created book
            mockMvc.perform(get("/api/v1/books/{id}", bookId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(bookId.intValue())))
                    .andExpect(jsonPath("$.title", is("Integration Test Book")))
                    .andExpect(jsonPath("$.author", is("Integration Author")))
                    .andExpect(jsonPath("$.price", is(25.99)))
                    .andExpect(jsonPath("$.isbn", is("978-0-111-11111-1")));

            // 3. UPDATE - Update the book
            BookUpdateRequest updateRequest = TestDataBuilder.aBookUpdateRequest()
                    .withTitle("Updated Integration Book")
                    .withAuthor("Updated Integration Author")
                    .withPrice("35.99")
                    .withIsbn("978-0-222-22222-2")
                    .withCategory("Updated Integration")
                    .withPages(350)
                    .withStockQuantity(20)
                    .build();

            mockMvc.perform(put("/api/v1/books/{id}", bookId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(bookId.intValue())))
                    .andExpect(jsonPath("$.title", is("Updated Integration Book")))
                    .andExpect(jsonPath("$.author", is("Updated Integration Author")))
                    .andExpect(jsonPath("$.price", is(35.99)))
                    .andExpect(jsonPath("$.isbn", is("978-0-222-22222-2")));

            // Verify update in database
            Book updatedBook = bookRepository.findById(bookId).orElse(null);
            assertThat(updatedBook).isNotNull();
            assertThat(updatedBook.getTitle()).isEqualTo("Updated Integration Book");
            assertThat(updatedBook.getPrice()).isEqualByComparingTo(new BigDecimal("35.99"));

            // 4. DELETE - Delete the book
            mockMvc.perform(delete("/api/v1/books/{id}", bookId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // Verify deletion
            assertThat(bookRepository.findById(bookId)).isEmpty();

            // 5. Verify book is gone
            mockMvc.perform(get("/api/v1/books/{id}", bookId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Search and Pagination Integration Tests")
    @Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    class SearchAndPaginationIntegrationTests {

        @BeforeEach
        void setUpSearchTests() {
            // Data will be reloaded by @Sql before each test
        }

        @Test
        @DisplayName("Should search books by title with pagination")
        void shouldSearchBooksByTitleWithPagination() throws Exception {
            mockMvc.perform(get("/api/v1/books/search")
                            .param("title", "Great")
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].title", containsString("Great")))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.size", is(10)))
                    .andExpect(jsonPath("$.number", is(0)));
        }

        @Test
        @DisplayName("Should search books by author with case insensitive matching")
        void shouldSearchBooksByAuthorWithCaseInsensitiveMatching() throws Exception {
            mockMvc.perform(get("/api/v1/books/search")
                            .param("author", "martin") // lowercase
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].author", containsStringIgnoringCase("Martin")));
        }

        @Test
        @DisplayName("Should search books by category")
        void shouldSearchBooksByCategory() throws Exception {
            mockMvc.perform(get("/api/v1/books/search")
                            .param("category", "Fiction")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                    .andExpect(jsonPath("$.content[*].category", everyItem(is("Fiction"))));
        }

        @Test
        @DisplayName("Should get books in stock only")
        void shouldGetBooksInStockOnly() throws Exception {
            mockMvc.perform(get("/api/v1/books/in-stock")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                    .andExpect(jsonPath("$.content[*].stockQuantity", everyItem(greaterThan(0))));
        }

        @Test
        @DisplayName("Should get book by ISBN")
        void shouldGetBookByIsbn() throws Exception {
            mockMvc.perform(get("/api/v1/books/isbn/{isbn}", "978-0-7432-7356-5")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isbn", is("978-0-7432-7356-5")))
                    .andExpect(jsonPath("$.title", is("The Great Gatsby")));
        }

        @Test
        @DisplayName("Should handle pagination correctly")
        void shouldHandlePaginationCorrectly() throws Exception {
            // Get first page
            mockMvc.perform(get("/api/v1/books")
                            .param("page", "0")
                            .param("size", "2")
                            .param("sort", "title")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.number", is(0)))
                    .andExpect(jsonPath("$.size", is(2)))
                    .andExpect(jsonPath("$.totalElements", greaterThan(2)))
                    .andExpect(jsonPath("$.last", is(false)));

            // Get second page
            mockMvc.perform(get("/api/v1/books")
                            .param("page", "1")
                            .param("size", "2")
                            .param("sort", "title")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(2))))
                    .andExpect(jsonPath("$.number", is(1)))
                    .andExpect(jsonPath("$.size", is(2)));
        }
    }

    @Nested
    @DisplayName("Business Logic Integration Tests")
    class BusinessLogicIntegrationTests {

        @Test
        @DisplayName("Should prevent duplicate ISBN creation")
        void shouldPreventDuplicateIsbnCreation() throws Exception {
            // Create first book
            BookCreateRequest firstBook = TestDataBuilder.aBookCreateRequest()
                    .withTitle("First Book")
                    .withIsbn("978-0-duplicate-isbn")
                    .build();

            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstBook)))
                    .andDo(print())
                    .andExpect(status().isCreated());

            // Try to create second book with same ISBN
            BookCreateRequest duplicateBook = TestDataBuilder.aBookCreateRequest()
                    .withTitle("Duplicate ISBN Book")
                    .withIsbn("978-0-duplicate-isbn") // Same ISBN
                    .build();

            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateBook)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should prevent updating to existing ISBN")
        void shouldPreventUpdatingToExistingIsbn() throws Exception {
            // Create two books with different ISBNs
            Book book1 = bookRepository.save(TestDataBuilder.aBook()
                    .withTitle("Book 1")
                    .withIsbn("978-0-111-11111-1")
                    .build());

            Book book2 = bookRepository.save(TestDataBuilder.aBook()
                    .withTitle("Book 2")
                    .withIsbn("978-0-222-22222-2")
                    .build());

            // Try to update book2 to use book1's ISBN
            BookUpdateRequest updateRequest = TestDataBuilder.aBookUpdateRequest()
                    .withTitle("Updated Book 2")
                    .withIsbn("978-0-111-11111-1") // book1's ISBN
                    .build();

            mockMvc.perform(put("/api/v1/books/{id}", book2.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle null ISBN gracefully")
        void shouldHandleNullIsbnGracefully() throws Exception {
            BookCreateRequest bookWithoutIsbn = TestDataBuilder.aBookCreateRequest()
                    .withTitle("Book Without ISBN")
                    .withIsbn(null)
                    .build();

            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bookWithoutIsbn)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title", is("Book Without ISBN")))
                    .andExpect(jsonPath("$.isbn").doesNotExist());
        }
    }

    @Nested
    @DisplayName("Error Handling Integration Tests")
    class ErrorHandlingIntegrationTests {

        @Test
        @DisplayName("Should return 404 for non-existent book")
        void shouldReturn404ForNonExistentBook() throws Exception {
            mockMvc.perform(get("/api/v1/books/{id}", 99999L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 for non-existent ISBN")
        void shouldReturn404ForNonExistentIsbn() throws Exception {
            mockMvc.perform(get("/api/v1/books/isbn/{isbn}", "978-0-000-00000-0")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should handle validation errors properly")
        void shouldHandleValidationErrorsProperly() throws Exception {
            // Create request with invalid data
            BookCreateRequest invalidBook = new BookCreateRequest();
            invalidBook.setTitle(""); // Empty title (should be not blank)
            invalidBook.setAuthor(""); // Empty author (should be not blank)
            invalidBook.setPrice(BigDecimal.ZERO); // Zero price (should be > 0)
            invalidBook.setPages(0); // Zero pages (should be >= 1)
            invalidBook.setStockQuantity(-1); // Negative stock (should be >= 0)

            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidBook)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle field length validation")
        void shouldHandleFieldLengthValidation() throws Exception {
            BookCreateRequest invalidBook = TestDataBuilder.aBookCreateRequest()
                    .withTitle("A".repeat(256)) // Exceeds max length of 255
                    .withAuthor("B".repeat(256)) // Exceeds max length of 255
                    .withIsbn("C".repeat(21)) // Exceeds max length of 20
                    .withCategory("D".repeat(101)) // Exceeds max length of 100
                    .withPublisher("E".repeat(256)) // Exceeds max length of 255
                    .withImageUrl("F".repeat(501)) // Exceeds max length of 500
                    .build();

            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidBook)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Database Integration Tests")
    class DatabaseIntegrationTests {

        @Test
        @DisplayName("Should persist and retrieve book with all fields")
        void shouldPersistAndRetrieveBookWithAllFields() throws Exception {
            BookCreateRequest fullBook = TestDataBuilder.aBookCreateRequest()
                    .withTitle("Complete Book")
                    .withAuthor("Complete Author")
                    .withPrice("99.99")
                    .withIsbn("978-0-complete-book")
                    .withDescription("A complete description of this book")
                    .withCategory("Complete Category")
                    .withPublisher("Complete Publisher")
                    .withPublicationDate(LocalDate.of(2023, 6, 15))
                    .withPages(500)
                    .withStockQuantity(25)
                    .withImageUrl("https://example.com/complete-book.jpg")
                    .build();

            String response = mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(fullBook)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Book createdBook = objectMapper.readValue(response, Book.class);

            // Verify all fields are persisted correctly
            Book retrievedBook = bookRepository.findById(createdBook.getId()).orElse(null);
            assertThat(retrievedBook).isNotNull();
            assertThat(retrievedBook.getTitle()).isEqualTo("Complete Book");
            assertThat(retrievedBook.getAuthor()).isEqualTo("Complete Author");
            assertThat(retrievedBook.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
            assertThat(retrievedBook.getIsbn()).isEqualTo("978-0-complete-book");
            assertThat(retrievedBook.getDescription()).isEqualTo("A complete description of this book");
            assertThat(retrievedBook.getCategory()).isEqualTo("Complete Category");
            assertThat(retrievedBook.getPublisher()).isEqualTo("Complete Publisher");
            assertThat(retrievedBook.getPublicationDate()).isEqualTo(LocalDate.of(2023, 6, 15));
            assertThat(retrievedBook.getPages()).isEqualTo(500);
            assertThat(retrievedBook.getStockQuantity()).isEqualTo(25);
            assertThat(retrievedBook.getImageUrl()).isEqualTo("https://example.com/complete-book.jpg");
            assertThat(retrievedBook.getCreatedAt()).isNotNull();
            assertThat(retrievedBook.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should update timestamps on modification")
        void shouldUpdateTimestampsOnModification() throws Exception {
            // Create a book
            Book book = bookRepository.save(TestDataBuilder.aBook()
                    .withTitle("Original Title")
                    .build());

            // Wait a moment to ensure timestamp difference
            Thread.sleep(10);

            // Update the book
            BookUpdateRequest updateRequest = TestDataBuilder.aBookUpdateRequest()
                    .withTitle("Updated Title")
                    .build();

            mockMvc.perform(put("/api/v1/books/{id}", book.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk());

            // Verify updated timestamp changed (with small delay to ensure different timestamp)
            Thread.sleep(10);
            Book updatedBook = bookRepository.findById(book.getId()).orElse(null);
            assertThat(updatedBook).isNotNull();
            assertThat(updatedBook.getCreatedAt()).isEqualTo(book.getCreatedAt());
            assertThat(updatedBook.getUpdatedAt()).isAfterOrEqualTo(book.getUpdatedAt());
        }
    }
}