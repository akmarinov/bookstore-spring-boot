-- Create books table
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    description TEXT,
    category VARCHAR(100),
    publisher VARCHAR(255),
    publication_date DATE,
    pages INTEGER,
    stock_quantity INTEGER DEFAULT 0,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_books_title (title),
    INDEX idx_books_author (author),
    INDEX idx_books_category (category),
    INDEX idx_books_isbn (isbn),
    INDEX idx_books_price (price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data
INSERT INTO books (title, author, price, isbn, description, category, publisher, publication_date, pages, stock_quantity, image_url) VALUES
('The Great Gatsby', 'F. Scott Fitzgerald', 12.99, '978-0-7432-7356-5', 'A classic American novel set in the Jazz Age', 'Fiction', 'Scribner', '1925-04-10', 180, 25, 'https://example.com/images/great-gatsby.jpg'),
('To Kill a Mockingbird', 'Harper Lee', 14.99, '978-0-06-112008-4', 'A gripping tale of racial injustice and childhood in the American South', 'Fiction', 'J.B. Lippincott & Co.', '1960-07-11', 376, 30, 'https://example.com/images/mockingbird.jpg'),
('1984', 'George Orwell', 13.99, '978-0-452-28423-4', 'A dystopian social science fiction novel', 'Science Fiction', 'Secker & Warburg', '1949-06-08', 328, 20, 'https://example.com/images/1984.jpg'),
('Pride and Prejudice', 'Jane Austen', 11.99, '978-0-14-143951-8', 'A romantic novel of manners', 'Romance', 'T. Egerton', '1813-01-28', 432, 15, 'https://example.com/images/pride-prejudice.jpg'),
('The Catcher in the Rye', 'J.D. Salinger', 13.50, '978-0-316-76948-0', 'A controversial novel about teenage rebellion', 'Fiction', 'Little, Brown and Company', '1951-07-16', 277, 22, 'https://example.com/images/catcher-rye.jpg'),
('Lord of the Rings', 'J.R.R. Tolkien', 29.99, '978-0-547-92822-7', 'Epic fantasy adventure in Middle-earth', 'Fantasy', 'George Allen & Unwin', '1954-07-29', 1216, 18, 'https://example.com/images/lotr.jpg'),
('Harry Potter and the Philosopher''s Stone', 'J.K. Rowling', 16.99, '978-0-7475-3269-9', 'The first book in the Harry Potter series', 'Fantasy', 'Bloomsbury', '1997-06-26', 223, 35, 'https://example.com/images/harry-potter-1.jpg'),
('The Hobbit', 'J.R.R. Tolkien', 15.99, '978-0-547-92822-8', 'A fantasy adventure story', 'Fantasy', 'George Allen & Unwin', '1937-09-21', 310, 28, 'https://example.com/images/hobbit.jpg'),
('Dune', 'Frank Herbert', 18.99, '978-0-441-17271-9', 'A science fiction masterpiece set on the desert planet Arrakis', 'Science Fiction', 'Chilton Books', '1965-08-01', 688, 12, 'https://example.com/images/dune.jpg'),
('The Chronicles of Narnia', 'C.S. Lewis', 24.99, '978-0-06-623851-4', 'Complete collection of the Narnia series', 'Fantasy', 'Geoffrey Bles', '1950-10-16', 767, 20, 'https://example.com/images/narnia.jpg');