-- Create additional schemas or configurations if needed
-- This script runs after user creation

-- Set default charset and collation for the database
ALTER DATABASE bookstore_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create a table to track initialization
CREATE TABLE IF NOT EXISTS bookstore_db.initialization_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    script_name VARCHAR(255) NOT NULL,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'SUCCESS'
);

-- Log this initialization
INSERT INTO bookstore_db.initialization_log (script_name, status) 
VALUES ('02-create-schema.sql', 'SUCCESS');

-- Display database configuration
SHOW VARIABLES LIKE 'character_set_database';
SHOW VARIABLES LIKE 'collation_database';