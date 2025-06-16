-- Test Data for Books table
INSERT INTO books (title, author, price, isbn, description, category, publisher, publication_date, pages, stock_quantity, image_url, created_at, updated_at) 
VALUES 
('The Great Gatsby', 'F. Scott Fitzgerald', 12.99, '978-0-7432-7356-5', 'A classic American novel', 'Fiction', 'Scribner', '1925-04-10', 180, 10, 'https://example.com/gatsby.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('To Kill a Mockingbird', 'Harper Lee', 14.99, '978-0-06-112008-4', 'A gripping tale of racial injustice and childhood innocence', 'Fiction', 'J. B. Lippincott & Co.', '1960-07-11', 281, 15, 'https://example.com/mockingbird.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('1984', 'George Orwell', 13.99, '978-0-452-28423-4', 'A dystopian social science fiction novel', 'Science Fiction', 'Secker & Warburg', '1949-06-08', 328, 8, 'https://example.com/1984.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Pride and Prejudice', 'Jane Austen', 11.99, '978-0-14-143951-8', 'A romantic novel of manners', 'Romance', 'T. Egerton', '1813-01-28', 432, 12, 'https://example.com/pride.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('The Catcher in the Rye', 'J.D. Salinger', 15.99, '978-0-316-76948-0', 'A controversial novel about teenage rebellion', 'Fiction', 'Little, Brown and Company', '1951-07-16', 277, 0, 'https://example.com/catcher.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Clean Code', 'Robert C. Martin', 49.99, '978-0-13-235088-4', 'A handbook of agile software craftsmanship', 'Technology', 'Prentice Hall', '2008-08-01', 464, 25, 'https://example.com/cleancode.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);