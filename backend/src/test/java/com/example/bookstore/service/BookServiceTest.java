package com.example.bookstore.service;

import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book testBook;
    private Book anotherBook;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testBook = TestDataBuilder.aBook()
                .withId(1L)
                .withTitle("Test Book")
                .withAuthor("Test Author")
                .withPrice("19.99")
                .withIsbn("978-0-123-45678-9")
                .build();

        anotherBook = TestDataBuilder.aBook()
                .withId(2L)
                .withTitle("Another Book")
                .withAuthor("Another Author")
                .withPrice("29.99")
                .withIsbn("978-0-123-45678-0")
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("Get All Books Tests")
    class GetAllBooksTests {

        @Test
        @DisplayName("Should return paginated books when books exist")
        void shouldReturnPaginatedBooksWhenBooksExist() {
            // Given
            List<Book> books = Arrays.asList(testBook, anotherBook);
            Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
            given(bookRepository.findAll(pageable)).willReturn(bookPage);

            // When
            Page<Book> result = bookService.getAllBooks(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(testBook, anotherBook);
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(bookRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no books exist")
        void shouldReturnEmptyPageWhenNoBooksExist() {
            // Given
            Page<Book> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
            given(bookRepository.findAll(pageable)).willReturn(emptyPage);

            // When
            Page<Book> result = bookService.getAllBooks(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            verify(bookRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Get Book By ID Tests")
    class GetBookByIdTests {

        @Test
        @DisplayName("Should return book when book exists")
        void shouldReturnBookWhenBookExists() {
            // Given
            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));

            // When
            Optional<Book> result = bookService.getBookById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testBook);
            verify(bookRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty optional when book does not exist")
        void shouldReturnEmptyOptionalWhenBookDoesNotExist() {
            // Given
            given(bookRepository.findById(99L)).willReturn(Optional.empty());

            // When
            Optional<Book> result = bookService.getBookById(99L);

            // Then
            assertThat(result).isEmpty();
            verify(bookRepository).findById(99L);
        }
    }

    @Nested
    @DisplayName("Get Book By ISBN Tests")
    class GetBookByIsbnTests {

        @Test
        @DisplayName("Should return book when ISBN exists")
        void shouldReturnBookWhenIsbnExists() {
            // Given
            String isbn = "978-0-123-45678-9";
            given(bookRepository.findByIsbn(isbn)).willReturn(Optional.of(testBook));

            // When
            Optional<Book> result = bookService.getBookByIsbn(isbn);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testBook);
            verify(bookRepository).findByIsbn(isbn);
        }

        @Test
        @DisplayName("Should return empty optional when ISBN does not exist")
        void shouldReturnEmptyOptionalWhenIsbnDoesNotExist() {
            // Given
            String isbn = "978-0-123-45678-1";
            given(bookRepository.findByIsbn(isbn)).willReturn(Optional.empty());

            // When
            Optional<Book> result = bookService.getBookByIsbn(isbn);

            // Then
            assertThat(result).isEmpty();
            verify(bookRepository).findByIsbn(isbn);
        }
    }

    @Nested
    @DisplayName("Create Book Tests")
    class CreateBookTests {

        @Test
        @DisplayName("Should create book successfully when valid data provided")
        void shouldCreateBookSuccessfullyWhenValidDataProvided() {
            // Given
            Book newBook = TestDataBuilder.aBook()
                    .withTitle("New Book")
                    .withIsbn("978-0-123-45678-1")
                    .build();
            Book savedBook = TestDataBuilder.aBook()
                    .withId(3L)
                    .withTitle("New Book")
                    .withIsbn("978-0-123-45678-1")
                    .build();

            given(bookRepository.findByIsbn("978-0-123-45678-1")).willReturn(Optional.empty());
            given(bookRepository.save(newBook)).willReturn(savedBook);

            // When
            Book result = bookService.createBook(newBook);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(3L);
            assertThat(result.getTitle()).isEqualTo("New Book");
            assertThat(result.getIsbn()).isEqualTo("978-0-123-45678-1");
            verify(bookRepository).findByIsbn("978-0-123-45678-1");
            verify(bookRepository).save(newBook);
        }

        @Test
        @DisplayName("Should throw exception when ISBN already exists")
        void shouldThrowExceptionWhenIsbnAlreadyExists() {
            // Given
            Book newBook = TestDataBuilder.aBook()
                    .withTitle("New Book")
                    .withIsbn("978-0-123-45678-9") // Existing ISBN
                    .build();

            given(bookRepository.findByIsbn("978-0-123-45678-9")).willReturn(Optional.of(testBook));

            // When & Then
            assertThatThrownBy(() -> bookService.createBook(newBook))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Book with ISBN " + newBook.getIsbn() + " already exists");

            verify(bookRepository).findByIsbn("978-0-123-45678-9");
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("Should create book successfully when ISBN is null")
        void shouldCreateBookSuccessfullyWhenIsbnIsNull() {
            // Given
            Book newBook = TestDataBuilder.aBook()
                    .withTitle("New Book")
                    .withIsbn(null)
                    .build();
            Book savedBook = TestDataBuilder.aBook()
                    .withId(3L)
                    .withTitle("New Book")
                    .withIsbn(null)
                    .build();

            given(bookRepository.save(newBook)).willReturn(savedBook);

            // When
            Book result = bookService.createBook(newBook);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(3L);
            assertThat(result.getTitle()).isEqualTo("New Book");
            assertThat(result.getIsbn()).isNull();
            verify(bookRepository, never()).findByIsbn(any());
            verify(bookRepository).save(newBook);
        }
    }

    @Nested
    @DisplayName("Update Book Tests")
    class UpdateBookTests {

        @Test
        @DisplayName("Should update book successfully when valid data provided")
        void shouldUpdateBookSuccessfullyWhenValidDataProvided() {
            // Given
            Book updateData = TestDataBuilder.aBook()
                    .withTitle("Updated Title")
                    .withAuthor("Updated Author")
                    .withPrice("39.99")
                    .withIsbn("978-0-123-45678-2")
                    .build();

            Book updatedBook = TestDataBuilder.aBook()
                    .withId(1L)
                    .withTitle("Updated Title")
                    .withAuthor("Updated Author")
                    .withPrice("39.99")
                    .withIsbn("978-0-123-45678-2")
                    .build();

            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(bookRepository.findByIsbn("978-0-123-45678-2")).willReturn(Optional.empty());
            given(bookRepository.save(any(Book.class))).willReturn(updatedBook);

            // When
            Book result = bookService.updateBook(1L, updateData);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Updated Title");
            assertThat(result.getAuthor()).isEqualTo("Updated Author");
            assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("39.99"));
            assertThat(result.getIsbn()).isEqualTo("978-0-123-45678-2");
            verify(bookRepository).findById(1L);
            verify(bookRepository).findByIsbn("978-0-123-45678-2");
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw BookNotFoundException when book does not exist")
        void shouldThrowBookNotFoundExceptionWhenBookDoesNotExist() {
            // Given
            Book updateData = TestDataBuilder.aBook().build();
            given(bookRepository.findById(99L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bookService.updateBook(99L, updateData))
                    .isInstanceOf(BookNotFoundException.class);

            verify(bookRepository).findById(99L);
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw exception when new ISBN already exists")
        void shouldThrowExceptionWhenNewIsbnAlreadyExists() {
            // Given
            Book updateData = TestDataBuilder.aBook()
                    .withIsbn("978-0-123-45678-0") // Another book's ISBN
                    .build();

            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(bookRepository.findByIsbn("978-0-123-45678-0")).willReturn(Optional.of(anotherBook));

            // When & Then
            assertThatThrownBy(() -> bookService.updateBook(1L, updateData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Book with ISBN " + updateData.getIsbn() + " already exists");

            verify(bookRepository).findById(1L);
            verify(bookRepository).findByIsbn("978-0-123-45678-0");
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("Should update book successfully when ISBN is not changed")
        void shouldUpdateBookSuccessfullyWhenIsbnIsNotChanged() {
            // Given
            Book updateData = TestDataBuilder.aBook()
                    .withTitle("Updated Title")
                    .withIsbn("978-0-123-45678-9") // Same ISBN
                    .build();

            Book updatedBook = TestDataBuilder.aBook()
                    .withId(1L)
                    .withTitle("Updated Title")
                    .withIsbn("978-0-123-45678-9")
                    .build();

            given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
            given(bookRepository.save(any(Book.class))).willReturn(updatedBook);

            // When
            Book result = bookService.updateBook(1L, updateData);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated Title");
            assertThat(result.getIsbn()).isEqualTo("978-0-123-45678-9");
            verify(bookRepository).findById(1L);
            verify(bookRepository, never()).findByIsbn(any());
            verify(bookRepository).save(any(Book.class));
        }
    }

    @Nested
    @DisplayName("Delete Book Tests")
    class DeleteBookTests {

        @Test
        @DisplayName("Should delete book successfully when book exists")
        void shouldDeleteBookSuccessfullyWhenBookExists() {
            // Given
            given(bookRepository.existsById(1L)).willReturn(true);
            willDoNothing().given(bookRepository).deleteById(1L);

            // When
            bookService.deleteBook(1L);

            // Then
            verify(bookRepository).existsById(1L);
            verify(bookRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw BookNotFoundException when book does not exist")
        void shouldThrowBookNotFoundExceptionWhenBookDoesNotExist() {
            // Given
            given(bookRepository.existsById(99L)).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> bookService.deleteBook(99L))
                    .isInstanceOf(BookNotFoundException.class);

            verify(bookRepository).existsById(99L);
            verify(bookRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Search Books Tests")
    class SearchBooksTests {

        @Test
        @DisplayName("Should search books by title successfully")
        void shouldSearchBooksByTitleSuccessfully() {
            // Given
            String title = "Test";
            List<Book> books = Arrays.asList(testBook);
            Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
            given(bookRepository.findByTitleContainingIgnoreCase(title, pageable)).willReturn(bookPage);

            // When
            Page<Book> result = bookService.searchBooksByTitle(title, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).contains(testBook);
            verify(bookRepository).findByTitleContainingIgnoreCase(title, pageable);
        }

        @Test
        @DisplayName("Should search books by author successfully")
        void shouldSearchBooksByAuthorSuccessfully() {
            // Given
            String author = "Test";
            List<Book> books = Arrays.asList(testBook);
            Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
            given(bookRepository.findByAuthorContainingIgnoreCase(author, pageable)).willReturn(bookPage);

            // When
            Page<Book> result = bookService.searchBooksByAuthor(author, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).contains(testBook);
            verify(bookRepository).findByAuthorContainingIgnoreCase(author, pageable);
        }

        @Test
        @DisplayName("Should search books by category successfully")
        void shouldSearchBooksByCategorySuccessfully() {
            // Given
            String category = "Fiction";
            List<Book> books = Arrays.asList(testBook);
            Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
            given(bookRepository.findByCategoryContainingIgnoreCase(category, pageable)).willReturn(bookPage);

            // When
            Page<Book> result = bookService.searchBooksByCategory(category, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).contains(testBook);
            verify(bookRepository).findByCategoryContainingIgnoreCase(category, pageable);
        }

        @Test
        @DisplayName("Should search books by title and author successfully")
        void shouldSearchBooksByTitleAndAuthorSuccessfully() {
            // Given
            String title = "Test";
            String author = "Author";
            List<Book> books = Arrays.asList(testBook);
            Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
            given(bookRepository.findByTitleAndAuthor(title, author, pageable)).willReturn(bookPage);

            // When
            Page<Book> result = bookService.searchBooks(title, author, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).contains(testBook);
            verify(bookRepository).findByTitleAndAuthor(title, author, pageable);
        }
    }

    @Nested
    @DisplayName("Get Books In Stock Tests")
    class GetBooksInStockTests {

        @Test
        @DisplayName("Should return books in stock successfully")
        void shouldReturnBooksInStockSuccessfully() {
            // Given
            List<Book> booksInStock = Arrays.asList(testBook, anotherBook);
            Page<Book> bookPage = new PageImpl<>(booksInStock, pageable, booksInStock.size());
            given(bookRepository.findByStockQuantityGreaterThan(0, pageable)).willReturn(bookPage);

            // When
            Page<Book> result = bookService.getBooksInStock(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(testBook, anotherBook);
            verify(bookRepository).findByStockQuantityGreaterThan(0, pageable);
        }

        @Test
        @DisplayName("Should return empty page when no books in stock")
        void shouldReturnEmptyPageWhenNoBooksInStock() {
            // Given
            Page<Book> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
            given(bookRepository.findByStockQuantityGreaterThan(0, pageable)).willReturn(emptyPage);

            // When
            Page<Book> result = bookService.getBooksInStock(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            verify(bookRepository).findByStockQuantityGreaterThan(0, pageable);
        }
    }
}