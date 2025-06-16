-- Create application user and grant necessary permissions
-- This script runs when MySQL container starts for the first time

-- Create the application user if it doesn't exist
CREATE USER IF NOT EXISTS 'bookstore'@'%' IDENTIFIED BY 'bookstore123';

-- Grant necessary permissions to the bookstore user
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore_db.* TO 'bookstore'@'%';
GRANT CREATE, DROP, INDEX, ALTER ON bookstore_db.* TO 'bookstore'@'%';
GRANT REFERENCES ON bookstore_db.* TO 'bookstore'@'%';

-- Grant permissions for Flyway migrations
GRANT CREATE, DROP ON bookstore_db.* TO 'bookstore'@'%';

-- Flush privileges to ensure changes take effect
FLUSH PRIVILEGES;

-- Display created users (for verification)
SELECT User, Host FROM mysql.user WHERE User = 'bookstore';