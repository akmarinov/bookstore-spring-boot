package com.example.bookstore.repository;

import com.example.bookstore.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    /**
     * Find books by title containing the given text (case-insensitive)
     */
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    /**
     * Find books by author containing the given text (case-insensitive)
     */
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);
    
    /**
     * Find books by category containing the given text (case-insensitive)
     */
    Page<Book> findByCategoryContainingIgnoreCase(String category, Pageable pageable);
    
    /**
     * Find book by ISBN
     */
    Optional<Book> findByIsbn(String isbn);
    
    /**
     * Find books by title and author (case-insensitive)
     */
    @Query("SELECT b FROM Book b WHERE " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')))")
    Page<Book> findByTitleAndAuthor(@Param("title") String title, 
                                   @Param("author") String author, 
                                   Pageable pageable);
    
    /**
     * Find books with stock quantity greater than zero
     */
    Page<Book> findByStockQuantityGreaterThan(Integer stockQuantity, Pageable pageable);
    
    /**
     * Count books with stock quantity greater than the specified value
     */
    long countByStockQuantityGreaterThan(Integer stockQuantity);
    
    /**
     * Count books by category
     */
    long countByCategory(String category);
    
    /**
     * Find books by title containing the given keyword (case-insensitive) - no pagination
     */
    java.util.List<Book> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Find books by author containing the given keyword (case-insensitive) - no pagination
     */
    java.util.List<Book> findByAuthorContainingIgnoreCase(String author);
    
    /**
     * Find books by category containing the given keyword (case-insensitive) - no pagination
     */
    java.util.List<Book> findByCategoryContainingIgnoreCase(String category);
    
    /**
     * Custom query to search books by title or author containing keywords - no pagination
     */
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    java.util.List<Book> searchByTitleOrAuthor(@Param("keyword") String keyword);
    
    /**
     * Custom query to search books by title or author containing keywords with pagination
     */
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchByTitleOrAuthorWithPagination(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Check if a book exists with the given title and author (case-insensitive)
     */
    boolean existsByTitleIgnoreCaseAndAuthorIgnoreCase(String title, String author);
    
    /**
     * Check if a book exists with the given title and author, excluding a specific ID
     */
    @Query("SELECT COUNT(b) > 0 FROM Book b WHERE " +
           "LOWER(b.title) = LOWER(:title) AND LOWER(b.author) = LOWER(:author) AND b.id != :excludeId")
    boolean existsByTitleAndAuthorExcludingId(@Param("title") String title, 
                                              @Param("author") String author, 
                                              @Param("excludeId") Long excludeId);
}