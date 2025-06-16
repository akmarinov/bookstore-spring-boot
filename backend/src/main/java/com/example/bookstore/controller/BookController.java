package com.example.bookstore.controller;

import com.example.bookstore.dto.BookCreateRequest;
import com.example.bookstore.dto.BookUpdateRequest;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.model.Book;
import com.example.bookstore.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books", description = "Book management APIs")
public class BookController {
    
    private final BookService bookService;
    
    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    
    @Operation(summary = "Get all books", description = "Retrieve a paginated list of all books")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved books",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<Book>> getAllBooks(
            @PageableDefault(size = 10, sort = "title") 
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        
        System.out.println("GET /api/v1/books - Fetching all books with pagination: " + pageable);
        Page<Book> books = bookService.getAllBooks(pageable);
        return ResponseEntity.ok(books);
    }
    
    @Operation(summary = "Get book by ID", description = "Retrieve a specific book by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the book",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Book.class))),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(
            @PathVariable @Parameter(description = "Book ID") Long id) {
        
        System.out.println("GET /api/v1/books/" + id + " - Fetching book by ID");
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        return ResponseEntity.ok(book);
    }
    
    @Operation(summary = "Create a new book", description = "Create a new book in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book created successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Book.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<Book> createBook(
            @Valid @RequestBody BookCreateRequest request) {
        
        System.out.println("POST /api/v1/books - Creating new book: " + request.getTitle());
        
        Book book = new Book();
        BeanUtils.copyProperties(request, book);
        
        Book createdBook = bookService.createBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }
    
    @Operation(summary = "Update a book", description = "Update an existing book by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book updated successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Book.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @PathVariable @Parameter(description = "Book ID") Long id,
            @Valid @RequestBody BookUpdateRequest request) {
        
        System.out.println("PUT /api/v1/books/" + id + " - Updating book");
        
        Book bookDetails = new Book();
        BeanUtils.copyProperties(request, bookDetails);
        
        Book updatedBook = bookService.updateBook(id, bookDetails);
        return ResponseEntity.ok(updatedBook);
    }
    
    @Operation(summary = "Delete a book", description = "Delete a book by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable @Parameter(description = "Book ID") Long id) {
        
        System.out.println("DELETE /api/v1/books/" + id + " - Deleting book");
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Search books by title", description = "Search for books containing the specified title")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching books",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<Book>> searchBooks(
            @RequestParam(required = false) @Parameter(description = "Title to search for") String title,
            @RequestParam(required = false) @Parameter(description = "Author to search for") String author,
            @RequestParam(required = false) @Parameter(description = "Category to search for") String category,
            @PageableDefault(size = 10, sort = "title") 
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        
        System.out.println("GET /api/v1/books/search - Searching books with title: " + title + 
                ", author: " + author + ", category: " + category);
        
        Page<Book> books;
        
        if (title != null && author != null) {
            books = bookService.searchBooks(title, author, pageable);
        } else if (title != null) {
            books = bookService.searchBooksByTitle(title, pageable);
        } else if (author != null) {
            books = bookService.searchBooksByAuthor(author, pageable);
        } else if (category != null) {
            books = bookService.searchBooksByCategory(category, pageable);
        } else {
            books = bookService.getAllBooks(pageable);
        }
        
        return ResponseEntity.ok(books);
    }
    
    @Operation(summary = "Get books in stock", description = "Retrieve books that are currently in stock")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved books in stock",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/in-stock")
    public ResponseEntity<Page<Book>> getBooksInStock(
            @PageableDefault(size = 10, sort = "title") 
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        
        System.out.println("GET /api/v1/books/in-stock - Fetching books in stock");
        Page<Book> books = bookService.getBooksInStock(pageable);
        return ResponseEntity.ok(books);
    }
    
    @Operation(summary = "Get book by ISBN", description = "Retrieve a specific book by its ISBN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the book",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Book.class))),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Book> getBookByIsbn(
            @PathVariable @Parameter(description = "Book ISBN") String isbn) {
        
        System.out.println("GET /api/v1/books/isbn/" + isbn + " - Fetching book by ISBN");
        Book book = bookService.getBookByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException("ISBN", isbn));
        return ResponseEntity.ok(book);
    }
}