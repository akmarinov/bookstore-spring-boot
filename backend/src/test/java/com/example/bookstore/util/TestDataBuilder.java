package com.example.bookstore.util;

import com.example.bookstore.dto.BookCreateRequest;
import com.example.bookstore.dto.BookUpdateRequest;
import com.example.bookstore.model.Book;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Test data builder utility class for creating test objects
 */
public class TestDataBuilder {

    public static class BookBuilder {
        private Book book;

        public BookBuilder() {
            this.book = new Book();
            // Set default values
            this.book.setTitle("Test Book");
            this.book.setAuthor("Test Author");
            this.book.setPrice(new BigDecimal("19.99"));
            this.book.setIsbn("978-0-123-45678-9");
            this.book.setDescription("Test description");
            this.book.setCategory("Fiction");
            this.book.setPublisher("Test Publisher");
            this.book.setPublicationDate(LocalDate.of(2023, 1, 1));
            this.book.setPages(200);
            this.book.setStockQuantity(10);
            this.book.setImageUrl("https://example.com/test-book.jpg");
            this.book.setCreatedAt(LocalDateTime.now());
            this.book.setUpdatedAt(LocalDateTime.now());
        }

        public BookBuilder withId(Long id) {
            this.book.setId(id);
            return this;
        }

        public BookBuilder withTitle(String title) {
            this.book.setTitle(title);
            return this;
        }

        public BookBuilder withAuthor(String author) {
            this.book.setAuthor(author);
            return this;
        }

        public BookBuilder withPrice(BigDecimal price) {
            this.book.setPrice(price);
            return this;
        }

        public BookBuilder withPrice(String price) {
            this.book.setPrice(new BigDecimal(price));
            return this;
        }

        public BookBuilder withIsbn(String isbn) {
            this.book.setIsbn(isbn);
            return this;
        }

        public BookBuilder withDescription(String description) {
            this.book.setDescription(description);
            return this;
        }

        public BookBuilder withCategory(String category) {
            this.book.setCategory(category);
            return this;
        }

        public BookBuilder withPublisher(String publisher) {
            this.book.setPublisher(publisher);
            return this;
        }

        public BookBuilder withPublicationDate(LocalDate publicationDate) {
            this.book.setPublicationDate(publicationDate);
            return this;
        }

        public BookBuilder withPages(Integer pages) {
            this.book.setPages(pages);
            return this;
        }

        public BookBuilder withStockQuantity(Integer stockQuantity) {
            this.book.setStockQuantity(stockQuantity);
            return this;
        }

        public BookBuilder withImageUrl(String imageUrl) {
            this.book.setImageUrl(imageUrl);
            return this;
        }

        public BookBuilder outOfStock() {
            this.book.setStockQuantity(0);
            return this;
        }

        public BookBuilder inStock() {
            this.book.setStockQuantity(10);
            return this;
        }

        public Book build() {
            return this.book;
        }
    }

    public static class BookCreateRequestBuilder {
        private BookCreateRequest request;

        public BookCreateRequestBuilder() {
            this.request = new BookCreateRequest();
            // Set default values
            this.request.setTitle("Test Book");
            this.request.setAuthor("Test Author");
            this.request.setPrice(new BigDecimal("19.99"));
            this.request.setIsbn("978-0-123-45678-9");
            this.request.setDescription("Test description");
            this.request.setCategory("Fiction");
            this.request.setPublisher("Test Publisher");
            this.request.setPublicationDate(LocalDate.of(2023, 1, 1));
            this.request.setPages(200);
            this.request.setStockQuantity(10);
            this.request.setImageUrl("https://example.com/test-book.jpg");
        }

        public BookCreateRequestBuilder withTitle(String title) {
            this.request.setTitle(title);
            return this;
        }

        public BookCreateRequestBuilder withAuthor(String author) {
            this.request.setAuthor(author);
            return this;
        }

        public BookCreateRequestBuilder withPrice(BigDecimal price) {
            this.request.setPrice(price);
            return this;
        }

        public BookCreateRequestBuilder withPrice(String price) {
            this.request.setPrice(new BigDecimal(price));
            return this;
        }

        public BookCreateRequestBuilder withIsbn(String isbn) {
            this.request.setIsbn(isbn);
            return this;
        }

        public BookCreateRequestBuilder withDescription(String description) {
            this.request.setDescription(description);
            return this;
        }

        public BookCreateRequestBuilder withCategory(String category) {
            this.request.setCategory(category);
            return this;
        }

        public BookCreateRequestBuilder withPublisher(String publisher) {
            this.request.setPublisher(publisher);
            return this;
        }

        public BookCreateRequestBuilder withPublicationDate(LocalDate publicationDate) {
            this.request.setPublicationDate(publicationDate);
            return this;
        }

        public BookCreateRequestBuilder withPages(Integer pages) {
            this.request.setPages(pages);
            return this;
        }

        public BookCreateRequestBuilder withStockQuantity(Integer stockQuantity) {
            this.request.setStockQuantity(stockQuantity);
            return this;
        }

        public BookCreateRequestBuilder withImageUrl(String imageUrl) {
            this.request.setImageUrl(imageUrl);
            return this;
        }

        public BookCreateRequest build() {
            return this.request;
        }
    }

    public static class BookUpdateRequestBuilder {
        private BookUpdateRequest request;

        public BookUpdateRequestBuilder() {
            this.request = new BookUpdateRequest();
            // Set default values
            this.request.setTitle("Updated Test Book");
            this.request.setAuthor("Updated Test Author");
            this.request.setPrice(new BigDecimal("29.99"));
            this.request.setIsbn("978-0-123-45678-0");
            this.request.setDescription("Updated test description");
            this.request.setCategory("Non-Fiction");
            this.request.setPublisher("Updated Test Publisher");
            this.request.setPublicationDate(LocalDate.of(2023, 6, 1));
            this.request.setPages(300);
            this.request.setStockQuantity(15);
            this.request.setImageUrl("https://example.com/updated-test-book.jpg");
        }

        public BookUpdateRequestBuilder withTitle(String title) {
            this.request.setTitle(title);
            return this;
        }

        public BookUpdateRequestBuilder withAuthor(String author) {
            this.request.setAuthor(author);
            return this;
        }

        public BookUpdateRequestBuilder withPrice(BigDecimal price) {
            this.request.setPrice(price);
            return this;
        }

        public BookUpdateRequestBuilder withPrice(String price) {
            this.request.setPrice(new BigDecimal(price));
            return this;
        }

        public BookUpdateRequestBuilder withIsbn(String isbn) {
            this.request.setIsbn(isbn);
            return this;
        }

        public BookUpdateRequestBuilder withDescription(String description) {
            this.request.setDescription(description);
            return this;
        }

        public BookUpdateRequestBuilder withCategory(String category) {
            this.request.setCategory(category);
            return this;
        }

        public BookUpdateRequestBuilder withPublisher(String publisher) {
            this.request.setPublisher(publisher);
            return this;
        }

        public BookUpdateRequestBuilder withPublicationDate(LocalDate publicationDate) {
            this.request.setPublicationDate(publicationDate);
            return this;
        }

        public BookUpdateRequestBuilder withPages(Integer pages) {
            this.request.setPages(pages);
            return this;
        }

        public BookUpdateRequestBuilder withStockQuantity(Integer stockQuantity) {
            this.request.setStockQuantity(stockQuantity);
            return this;
        }

        public BookUpdateRequestBuilder withImageUrl(String imageUrl) {
            this.request.setImageUrl(imageUrl);
            return this;
        }

        public BookUpdateRequest build() {
            return this.request;
        }
    }

    // Static factory methods
    public static BookBuilder aBook() {
        return new BookBuilder();
    }

    public static BookCreateRequestBuilder aBookCreateRequest() {
        return new BookCreateRequestBuilder();
    }

    public static BookUpdateRequestBuilder aBookUpdateRequest() {
        return new BookUpdateRequestBuilder();
    }

    // Convenience methods for common test scenarios
    public static Book createTestBook() {
        return aBook().build();
    }

    public static Book createTestBookWithId(Long id) {
        return aBook().withId(id).build();
    }

    public static Book createTestBookOutOfStock() {
        return aBook().outOfStock().build();
    }

    public static BookCreateRequest createTestBookCreateRequest() {
        return aBookCreateRequest().build();
    }

    public static BookUpdateRequest createTestBookUpdateRequest() {
        return aBookUpdateRequest().build();
    }
}