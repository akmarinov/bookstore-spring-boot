package com.example.bookstore.config;

import com.example.bookstore.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom endpoint for book statistics
 */
@RestController
public class BookStatsEndpoint {

    private final BookRepository bookRepository;

    @Autowired
    public BookStatsEndpoint(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping("/actuator/bookstats")
    public Map<String, Object> bookStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalBooks = bookRepository.count();
            long booksInStock = bookRepository.countByStockQuantityGreaterThan(0);
            long booksOutOfStock = totalBooks - booksInStock;
            
            stats.put("totalBooks", totalBooks);
            stats.put("booksInStock", booksInStock);
            stats.put("booksOutOfStock", booksOutOfStock);
            
            // Add percentage calculations
            if (totalBooks > 0) {
                double inStockPercentage = (double) booksInStock / totalBooks * 100;
                double outOfStockPercentage = (double) booksOutOfStock / totalBooks * 100;
                
                stats.put("inStockPercentage", String.format("%.2f%%", inStockPercentage));
                stats.put("outOfStockPercentage", String.format("%.2f%%", outOfStockPercentage));
            }
            
            stats.put("status", "healthy");
            
        } catch (Exception e) {
            stats.put("status", "error");
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
}