package com.example.bookstore.controller;

import com.example.bookstore.dto.BookCreateRequest;
import com.example.bookstore.dto.BookUpdateRequest;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.model.Book;
import com.example.bookstore.service.BookService;
import com.example.bookstore.util.TestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@ActiveProfiles("test")
@DisplayName("BookController Tests")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private Book testBook;
    private Book anotherBook;
    private BookCreateRequest createRequest;
    private BookUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testBook = TestDataBuilder.aBook()
                .withId(1L)
                .withTitle("Test Book")
                .withAuthor("Test Author")
                .withPrice("19.99")
                .withIsbn("978-0-123-45678-9")
                .withCategory("Fiction")
                .withPublisher("Test Publisher")
                .withPublicationDate(LocalDate.of(2023, 1, 1))
                .withPages(200)
                .withStockQuantity(10)
                .build();

        anotherBook = TestDataBuilder.aBook()
                .withId(2L)
                .withTitle("Another Book")
                .withAuthor("Another Author")
                .withPrice("29.99")
                .withIsbn("978-0-123-45678-0")
                .build();

        createRequest = TestDataBuilder.aBookCreateRequest()
                .withTitle("New Book")
                .withAuthor("New Author")
                .withPrice("39.99")
                .withIsbn("978-0-123-45678-1")
                .build();

        updateRequest = TestDataBuilder.aBookUpdateRequest()
                .withTitle("Updated Book")
                .withAuthor("Updated Author")
                .withPrice("49.99")
                .withIsbn("978-0-123-45678-2")
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/books - Get All Books")
    class GetAllBooksTests {

        @Test
        @DisplayName("Should return paginated books successfully")
        void shouldReturnPaginatedBooksSuccessfully() throws Exception {
            // Given
            List<Book> books = Arrays.asList(testBook, anotherBook);
            Page<Book> bookPage = new PageImpl<>(books, PageRequest.of(0, 10), books.size());
            given(bookService.getAllBooks(any(Pageable.class))).willReturn(bookPage);

            // When & Then
            mockMvc.perform(get("/api/v1/books")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "title")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].id", is(1)))
                    .andExpect(jsonPath("$.content[0].title", is("Test Book")))
                    .andExpect(jsonPath("$.content[0].author", is("Test Author")))
                    .andExpect(jsonPath("$.content[0].price", is(19.99)))
                    .andExpect(jsonPath("$.content[1].id", is(2)))
                    .andExpect(jsonPath("$.content[1].title", is("Another Book")))
                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.size", is(10)))
                    .andExpect(jsonPath("$.number", is(0)));

            verify(bookService).getAllBooks(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no books exist")
        void shouldReturnEmptyPageWhenNoBooksExist() throws Exception {
            // Given
            Page<Book> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
            given(bookService.getAllBooks(any(Pageable.class))).willReturn(emptyPage);

            // When & Then
            mockMvc.perform(get("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements", is(0)));

            verify(bookService).getAllBooks(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/books/{id} - Get Book By ID")
    class GetBookByIdTests {

        @Test
        @DisplayName("Should return book when book exists")
        void shouldReturnBookWhenBookExists() throws Exception {
            // Given
            given(bookService.getBookById(1L)).willReturn(Optional.of(testBook));

            // When & Then
            mockMvc.perform(get("/api/v1/books/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.title", is("Test Book")))
                    .andExpect(jsonPath("$.author", is("Test Author")))
                    .andExpect(jsonPath("$.price", is(19.99)))
                    .andExpect(jsonPath("$.isbn", is("978-0-123-45678-9")))
                    .andExpect(jsonPath("$.category", is("Fiction")))
                    .andExpect(jsonPath("$.stockQuantity", is(10)));

            verify(bookService).getBookById(1L);
        }

        @Test
        @DisplayName("Should return 404 when book does not exist")
        void shouldReturn404WhenBookDoesNotExist() throws Exception {
            // Given
            given(bookService.getBookById(99L)).willReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/v1/books/{id}", 99L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(bookService).getBookById(99L);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/books - Create Book")
    class CreateBookTests {

        @Test
        @DisplayName("Should create book successfully with valid data")
        void shouldCreateBookSuccessfullyWithValidData() throws Exception {
            // Given
            Book createdBook = TestDataBuilder.aBook()
                    .withId(3L)
                    .withTitle("New Book")
                    .withAuthor("New Author")
                    .withPrice("39.99")
                    .withIsbn("978-0-123-45678-1")
                    .build();

            given(bookService.createBook(any(Book.class))).willReturn(createdBook);

            // When & Then
            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(3)))
                    .andExpect(jsonPath("$.title", is("New Book")))
                    .andExpect(jsonPath("$.author", is("New Author")))
                    .andExpect(jsonPath("$.price", is(39.99)))
                    .andExpect(jsonPath("$.isbn", is("978-0-123-45678-1")));

            verify(bookService).createBook(any(Book.class));
        }

        @Test
        @DisplayName("Should return 400 when required fields are missing")
        void shouldReturn400WhenRequiredFieldsAreMissing() throws Exception {
            // Given
            BookCreateRequest invalidRequest = new BookCreateRequest();
            // Leave title and author blank (required fields)

            // When & Then
            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).createBook(any(Book.class));
        }

        @Test
        @DisplayName("Should return 400 when price is invalid")
        void shouldReturn400WhenPriceIsInvalid() throws Exception {
            // Given
            BookCreateRequest invalidRequest = TestDataBuilder.aBookCreateRequest()
                    .withPrice(BigDecimal.ZERO) // Invalid price (must be > 0)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).createBook(any(Book.class));
        }

        @Test
        @DisplayName("Should return 400 when title exceeds maximum length")
        void shouldReturn400WhenTitleExceedsMaximumLength() throws Exception {
            // Given
            String longTitle = "A".repeat(256); // Exceeds 255 character limit
            BookCreateRequest invalidRequest = TestDataBuilder.aBookCreateRequest()
                    .withTitle(longTitle)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).createBook(any(Book.class));
        }

        @Test
        @DisplayName("Should return 400 when stock quantity is negative")
        void shouldReturn400WhenStockQuantityIsNegative() throws Exception {
            // Given
            BookCreateRequest invalidRequest = TestDataBuilder.aBookCreateRequest()
                    .withStockQuantity(-1) // Invalid negative stock
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).createBook(any(Book.class));
        }

        @Test
        @DisplayName("Should return 400 when pages is less than 1")
        void shouldReturn400WhenPagesIsLessThan1() throws Exception {
            // Given
            BookCreateRequest invalidRequest = TestDataBuilder.aBookCreateRequest()
                    .withPages(0) // Invalid pages count
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).createBook(any(Book.class));
        }

        @Test
        @DisplayName("Should return 400 when service throws IllegalArgumentException")
        void shouldReturn400WhenServiceThrowsIllegalArgumentException() throws Exception {
            // Given
            given(bookService.createBook(any(Book.class)))
                    .willThrow(new IllegalArgumentException("ISBN already exists"));

            // When & Then
            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(bookService).createBook(any(Book.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/books/{id} - Update Book")
    class UpdateBookTests {

        @Test
        @DisplayName("Should update book successfully with valid data")
        void shouldUpdateBookSuccessfullyWithValidData() throws Exception {
            // Given
            Book updatedBook = TestDataBuilder.aBook()
                    .withId(1L)
                    .withTitle("Updated Book")
                    .withAuthor("Updated Author")
                    .withPrice("49.99")
                    .withIsbn("978-0-123-45678-2")
                    .build();

            given(bookService.updateBook(eq(1L), any(Book.class))).willReturn(updatedBook);

            // When & Then
            mockMvc.perform(put("/api/v1/books/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.title", is("Updated Book")))
                    .andExpect(jsonPath("$.author", is("Updated Author")))
                    .andExpect(jsonPath("$.price", is(49.99)))
                    .andExpect(jsonPath("$.isbn", is("978-0-123-45678-2")));

            verify(bookService).updateBook(eq(1L), any(Book.class));
        }

        @Test
        @DisplayName("Should return 404 when book does not exist")
        void shouldReturn404WhenBookDoesNotExist() throws Exception {
            // Given
            given(bookService.updateBook(eq(99L), any(Book.class)))
                    .willThrow(new BookNotFoundException(99L));

            // When & Then
            mockMvc.perform(put("/api/v1/books/{id}", 99L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(bookService).updateBook(eq(99L), any(Book.class));
        }

        @Test
        @DisplayName("Should return 400 when update data is invalid")
        void shouldReturn400WhenUpdateDataIsInvalid() throws Exception {
            // Given
            BookUpdateRequest invalidRequest = new BookUpdateRequest();
            // Leave required fields blank

            // When & Then
            mockMvc.perform(put("/api/v1/books/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).updateBook(any(Long.class), any(Book.class));
        }

        @Test
        @DisplayName("Should return 400 when service throws IllegalArgumentException")
        void shouldReturn400WhenServiceThrowsIllegalArgumentException() throws Exception {
            // Given
            given(bookService.updateBook(eq(1L), any(Book.class)))
                    .willThrow(new IllegalArgumentException("ISBN already exists"));

            // When & Then
            mockMvc.perform(put("/api/v1/books/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(bookService).updateBook(eq(1L), any(Book.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/books/{id} - Delete Book")
    class DeleteBookTests {

        @Test
        @DisplayName("Should delete book successfully when book exists")
        void shouldDeleteBookSuccessfullyWhenBookExists() throws Exception {
            // Given
            willDoNothing().given(bookService).deleteBook(1L);

            // When & Then
            mockMvc.perform(delete("/api/v1/books/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(bookService).deleteBook(1L);
        }

        @Test
        @DisplayName("Should return 404 when book does not exist")
        void shouldReturn404WhenBookDoesNotExist() throws Exception {
            // Given
            willThrow(new BookNotFoundException(99L)).given(bookService).deleteBook(99L);

            // When & Then
            mockMvc.perform(delete("/api/v1/books/{id}", 99L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(bookService).deleteBook(99L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/books/search - Search Books")
    class SearchBooksTests {

        @Test
        @DisplayName("Should search books by title successfully")
        void shouldSearchBooksByTitleSuccessfully() throws Exception {
            // Given
            List<Book> books = Arrays.asList(testBook);
            Page<Book> bookPage = new PageImpl<>(books, PageRequest.of(0, 10), books.size());
            given(bookService.searchBooksByTitle(eq("Test"), any(Pageable.class))).willReturn(bookPage);

            // When & Then
            mockMvc.perform(get("/api/v1/books/search")
                            .param("title", "Test")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].title", is("Test Book")));

            verify(bookService).searchBooksByTitle(eq("Test"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search books by author successfully")
        void shouldSearchBooksByAuthorSuccessfully() throws Exception {
            // Given
            List<Book> books = Arrays.asList(testBook);
            Page<Book> bookPage = new PageImpl<>(books, PageRequest.of(0, 10), books.size());
            given(bookService.searchBooksByAuthor(eq("Test"), any(Pageable.class))).willReturn(bookPage);

            // When & Then
            mockMvc.perform(get("/api/v1/books/search")
                            .param("author", "Test")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].author", is("Test Author")));

            verify(bookService).searchBooksByAuthor(eq("Test"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search books by category successfully")
        void shouldSearchBooksByCategorySuccessfully() throws Exception {
            // Given
            List<Book> books = Arrays.asList(testBook);
            Page<Book> bookPage = new PageImpl<>(books, PageRequest.of(0, 10), books.size());
            given(bookService.searchBooksByCategory(eq("Fiction"), any(Pageable.class))).willReturn(bookPage);

            // When & Then
            mockMvc.perform(get("/api/v1/books/search")
                            .param("category", "Fiction")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].category", is("Fiction")));

            verify(bookService).searchBooksByCategory(eq("Fiction"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search books by title and author successfully")
        void shouldSearchBooksByTitleAndAuthorSuccessfully() throws Exception {
            // Given
            List<Book> books = Arrays.asList(testBook);
            Page<Book> bookPage = new PageImpl<>(books, PageRequest.of(0, 10), books.size());
            given(bookService.searchBooks(eq("Test"), eq("Author"), any(Pageable.class))).willReturn(bookPage);

            // When & Then
            mockMvc.perform(get("/api/v1/books/search")
                            .param("title", "Test")
                            .param("author", "Author")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].title", is("Test Book")))
                    .andExpect(jsonPath("$.content[0].author", is("Test Author")));

            verify(bookService).searchBooks(eq("Test"), eq("Author"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return all books when no search parameters provided")
        void shouldReturnAllBooksWhenNoSearchParametersProvided() throws Exception {
            // Given
            List<Book> books = Arrays.asList(testBook, anotherBook);
            Page<Book> bookPage = new PageImpl<>(books, PageRequest.of(0, 10), books.size());
            given(bookService.getAllBooks(any(Pageable.class))).willReturn(bookPage);

            // When & Then
            mockMvc.perform(get("/api/v1/books/search")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));

            verify(bookService).getAllBooks(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/books/in-stock - Get Books In Stock")
    class GetBooksInStockTests {

        @Test
        @DisplayName("Should return books in stock successfully")
        void shouldReturnBooksInStockSuccessfully() throws Exception {
            // Given
            List<Book> booksInStock = Arrays.asList(testBook, anotherBook);
            Page<Book> bookPage = new PageImpl<>(booksInStock, PageRequest.of(0, 10), booksInStock.size());
            given(bookService.getBooksInStock(any(Pageable.class))).willReturn(bookPage);

            // When & Then
            mockMvc.perform(get("/api/v1/books/in-stock")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].stockQuantity", greaterThan(0)))
                    .andExpect(jsonPath("$.content[1].stockQuantity", greaterThan(0)));

            verify(bookService).getBooksInStock(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/books/isbn/{isbn} - Get Book By ISBN")
    class GetBookByIsbnTests {

        @Test
        @DisplayName("Should return book when ISBN exists")
        void shouldReturnBookWhenIsbnExists() throws Exception {
            // Given
            String isbn = "978-0-123-45678-9";
            given(bookService.getBookByIsbn(isbn)).willReturn(Optional.of(testBook));

            // When & Then
            mockMvc.perform(get("/api/v1/books/isbn/{isbn}", isbn)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isbn", is(isbn)))
                    .andExpect(jsonPath("$.title", is("Test Book")))
                    .andExpect(jsonPath("$.author", is("Test Author")));

            verify(bookService).getBookByIsbn(isbn);
        }

        @Test
        @DisplayName("Should return 404 when ISBN does not exist")
        void shouldReturn404WhenIsbnDoesNotExist() throws Exception {
            // Given
            String isbn = "978-0-000-00000-0";
            given(bookService.getBookByIsbn(isbn)).willReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/v1/books/isbn/{isbn}", isbn)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(bookService).getBookByIsbn(isbn);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle malformed JSON gracefully")
        void shouldHandleMalformedJsonGracefully() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).createBook(any(Book.class));
        }

        @Test
        @DisplayName("Should handle invalid path variables gracefully")
        void shouldHandleInvalidPathVariablesGracefully() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/books/{id}", "invalid-id")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).getBookById(any(Long.class));
        }
    }
}