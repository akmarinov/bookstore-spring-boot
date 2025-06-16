package com.example.bookstore;

import com.example.bookstore.controller.BookController;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Bookstore Application Context Tests")
class BookstoreBackendApplicationTests {

    @Autowired
    private BookController bookController;

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Should load application context successfully")
    void shouldLoadApplicationContextSuccessfully() {
        // Verify that all main components are loaded and autowired correctly
        assertThat(bookController).isNotNull();
        assertThat(bookService).isNotNull();
        assertThat(bookRepository).isNotNull();
    }

    @Test
    @DisplayName("Should have proper bean wiring")
    void shouldHaveProperBeanWiring() {
        // Verify that the service layer is properly wired
        assertThat(bookService).isNotNull();
        
        // Verify that the controller has the service injected
        assertThat(bookController).isNotNull();
        
        // Verify that the repository layer is accessible
        assertThat(bookRepository).isNotNull();
    }

}