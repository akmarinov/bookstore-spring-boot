package com.example.bookstore.util;

/**
 * Mock implementation of SecurityUtils for testing purposes
 */
public class MockSecurityUtils {
    
    public static String sanitizeUserInput(String input) {
        return input; // For testing, just return the input as-is
    }
    
    public static String sanitizeHtml(String input) {
        return input; // For testing, just return the input as-is
    }
}