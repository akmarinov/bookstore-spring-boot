package com.example.bookstore.exception;

/**
 * Exception thrown when a book is not found
 */
public class BookNotFoundException extends RuntimeException {
    
    public BookNotFoundException(String message) {
        super(message);
    }
    
    public BookNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BookNotFoundException(Long bookId) {
        super("Book not found with ID: " + bookId);
    }
    
    public BookNotFoundException(String field, String value) {
        super("Book not found with " + field + ": " + value);
    }
}