package com.example.bookstore.service;

import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class BookService {
    
    private final BookRepository bookRepository;
    
    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    /**
     * Get all books with pagination
     */
    @Transactional(readOnly = true)
    public Page<Book> getAllBooks(Pageable pageable) {
        System.out.println("Fetching all books with pagination: " + pageable);
        return bookRepository.findAll(pageable);
    }
    
    /**
     * Get book by ID
     */
    @Transactional(readOnly = true)
    public Optional<Book> getBookById(Long id) {
        System.out.println("Fetching book with ID: " + id);
        return bookRepository.findById(id);
    }
    
    /**
     * Get book by ISBN
     */
    @Transactional(readOnly = true)
    public Optional<Book> getBookByIsbn(String isbn) {
        System.out.println("Fetching book with ISBN: " + isbn);
        return bookRepository.findByIsbn(isbn);
    }
    
    /**
     * Create a new book
     */
    public Book createBook(Book book) {
        System.out.println("Creating new book: " + book.getTitle());
        
        // Check if ISBN already exists
        if (book.getIsbn() != null && bookRepository.findByIsbn(book.getIsbn()).isPresent()) {
            throw new IllegalArgumentException("Book with ISBN " + book.getIsbn() + " already exists");
        }
        
        return bookRepository.save(book);
    }
    
    /**
     * Update an existing book
     */
    public Book updateBook(Long id, Book bookDetails) {
        System.out.println("Updating book with ID: " + id);
        
        return bookRepository.findById(id)
                .map(existingBook -> {
                    // Check if ISBN is being changed and if new ISBN already exists
                    if (bookDetails.getIsbn() != null && 
                        !bookDetails.getIsbn().equals(existingBook.getIsbn()) &&
                        bookRepository.findByIsbn(bookDetails.getIsbn()).isPresent()) {
                        throw new IllegalArgumentException("Book with ISBN " + bookDetails.getIsbn() + " already exists");
                    }
                    
                    // Update fields
                    existingBook.setTitle(bookDetails.getTitle());
                    existingBook.setAuthor(bookDetails.getAuthor());
                    existingBook.setPrice(bookDetails.getPrice());
                    existingBook.setIsbn(bookDetails.getIsbn());
                    existingBook.setDescription(bookDetails.getDescription());
                    existingBook.setCategory(bookDetails.getCategory());
                    existingBook.setPublisher(bookDetails.getPublisher());
                    existingBook.setPublicationDate(bookDetails.getPublicationDate());
                    existingBook.setPages(bookDetails.getPages());
                    existingBook.setStockQuantity(bookDetails.getStockQuantity());
                    existingBook.setImageUrl(bookDetails.getImageUrl());
                    
                    return bookRepository.save(existingBook);
                })
                .orElseThrow(() -> new BookNotFoundException(id));
    }
    
    /**
     * Delete a book
     */
    public void deleteBook(Long id) {
        System.out.println("Deleting book with ID: " + id);
        
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        
        bookRepository.deleteById(id);
    }
    
    /**
     * Search books by title
     */
    @Transactional(readOnly = true)
    public Page<Book> searchBooksByTitle(String title, Pageable pageable) {
        System.out.println("Searching books by title: " + title);
        return bookRepository.findByTitleContainingIgnoreCase(title, pageable);
    }
    
    /**
     * Search books by author
     */
    @Transactional(readOnly = true)
    public Page<Book> searchBooksByAuthor(String author, Pageable pageable) {
        System.out.println("Searching books by author: " + author);
        return bookRepository.findByAuthorContainingIgnoreCase(author, pageable);
    }
    
    /**
     * Search books by category
     */
    @Transactional(readOnly = true)
    public Page<Book> searchBooksByCategory(String category, Pageable pageable) {
        System.out.println("Searching books by category: " + category);
        return bookRepository.findByCategoryIgnoreCase(category, pageable);
    }
    
    /**
     * Search books by title and/or author
     */
    @Transactional(readOnly = true)
    public Page<Book> searchBooks(String title, String author, Pageable pageable) {
        System.out.println("Searching books by title: " + title + " and author: " + author);
        return bookRepository.findByTitleAndAuthor(title, author, pageable);
    }
    
    /**
     * Get books in stock
     */
    @Transactional(readOnly = true)
    public Page<Book> getBooksInStock(Pageable pageable) {
        System.out.println("Fetching books in stock");
        return bookRepository.findByStockQuantityGreaterThan(0, pageable);
    }
}