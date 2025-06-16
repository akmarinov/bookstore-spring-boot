package com.example.bookstore.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects custom metrics for book operations
 */
@Component
public class BookMetricsCollector {

    private final MeterRegistry meterRegistry;
    
    // Counters for tracking operations
    private Counter bookCreatedCounter;
    private Counter bookUpdatedCounter;
    private Counter bookDeletedCounter;
    private Counter bookViewedCounter;
    private Counter bookSearchCounter;
    
    // Timers for tracking response times
    private Timer bookCreateTimer;
    private Timer bookUpdateTimer;
    private Timer bookDeleteTimer;
    private Timer bookSearchTimer;
    private Timer bookFetchTimer;
    
    // Gauges for real-time metrics
    private AtomicLong activeBookOperations = new AtomicLong(0);
    private AtomicLong totalBooksInSystem = new AtomicLong(0);

    @Autowired
    public BookMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void initializeMetrics() {
        // Initialize counters
        bookCreatedCounter = Counter.builder("bookstore.books.created")
                .description("Number of books created")
                .register(meterRegistry);
                
        bookUpdatedCounter = Counter.builder("bookstore.books.updated")
                .description("Number of books updated")
                .register(meterRegistry);
                
        bookDeletedCounter = Counter.builder("bookstore.books.deleted")
                .description("Number of books deleted")
                .register(meterRegistry);
                
        bookViewedCounter = Counter.builder("bookstore.books.viewed")
                .description("Number of book views")
                .register(meterRegistry);
                
        bookSearchCounter = Counter.builder("bookstore.books.searched")
                .description("Number of book searches performed")
                .register(meterRegistry);

        // Initialize timers
        bookCreateTimer = Timer.builder("bookstore.books.create.duration")
                .description("Time taken to create a book")
                .register(meterRegistry);
                
        bookUpdateTimer = Timer.builder("bookstore.books.update.duration")
                .description("Time taken to update a book")
                .register(meterRegistry);
                
        bookDeleteTimer = Timer.builder("bookstore.books.delete.duration")
                .description("Time taken to delete a book")
                .register(meterRegistry);
                
        bookSearchTimer = Timer.builder("bookstore.books.search.duration")
                .description("Time taken to search books")
                .register(meterRegistry);
                
        bookFetchTimer = Timer.builder("bookstore.books.fetch.duration")
                .description("Time taken to fetch books")
                .register(meterRegistry);

        // Initialize gauges
        Gauge.builder("bookstore.books.active.operations", activeBookOperations, AtomicLong::get)
                .description("Number of active book operations")
                .register(meterRegistry);
                
        Gauge.builder("bookstore.books.total.count", totalBooksInSystem, AtomicLong::get)
                .description("Total number of books in the system")
                .register(meterRegistry);
    }

    // Counter increment methods
    public void incrementBookCreated() {
        bookCreatedCounter.increment();
    }

    public void incrementBookUpdated() {
        bookUpdatedCounter.increment();
    }

    public void incrementBookDeleted() {
        bookDeletedCounter.increment();
    }

    public void incrementBookViewed() {
        bookViewedCounter.increment();
    }

    public void incrementBookSearched() {
        bookSearchCounter.increment();
    }

    // Timer methods
    public Timer.Sample startBookCreateTimer() {
        activeBookOperations.incrementAndGet();
        return Timer.start(meterRegistry);
    }

    public void stopBookCreateTimer(Timer.Sample sample) {
        sample.stop(bookCreateTimer);
        activeBookOperations.decrementAndGet();
    }

    public Timer.Sample startBookUpdateTimer() {
        activeBookOperations.incrementAndGet();
        return Timer.start(meterRegistry);
    }

    public void stopBookUpdateTimer(Timer.Sample sample) {
        sample.stop(bookUpdateTimer);
        activeBookOperations.decrementAndGet();
    }

    public Timer.Sample startBookDeleteTimer() {
        activeBookOperations.incrementAndGet();
        return Timer.start(meterRegistry);
    }

    public void stopBookDeleteTimer(Timer.Sample sample) {
        sample.stop(bookDeleteTimer);
        activeBookOperations.decrementAndGet();
    }

    public Timer.Sample startBookSearchTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopBookSearchTimer(Timer.Sample sample) {
        sample.stop(bookSearchTimer);
    }

    public Timer.Sample startBookFetchTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopBookFetchTimer(Timer.Sample sample) {
        sample.stop(bookFetchTimer);
    }

    // Gauge update methods
    public void updateTotalBooksCount(long count) {
        totalBooksInSystem.set(count);
    }

    public void recordCustomMetric(String metricName, String description, double value) {
        Gauge.builder("bookstore.custom." + metricName, () -> value)
                .description(description)
                .register(meterRegistry);
    }
}