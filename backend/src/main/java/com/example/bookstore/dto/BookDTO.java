package com.example.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Book entity
 * Used for API responses and general book data transfer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    
    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
    private String isbn;
    private String description;
    private String category;
    private String publisher;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publicationDate;
    
    private Integer pages;
    private Integer stockQuantity;
    private String imageUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}