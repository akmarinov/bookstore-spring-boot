#!/bin/bash

echo "🧪 Quick Test of Bookstore Application"
echo "====================================="

# Set Java environment
export JAVA_HOME="/opt/homebrew/opt/openjdk@11"
export PATH="$JAVA_HOME/bin:$PATH"

echo "📋 Checking Prerequisites..."
echo "Java version:"
java -version 2>&1 | head -1

echo "📊 MySQL status:"
brew services list | grep mysql

echo "📁 Project structure:"
ls -la

echo ""
echo "🔧 Attempting to compile Java files manually..."

# Create temporary classpath with downloaded dependencies
CLASSPATH=""
for jar in $(find ~/.m2/repository -name "*.jar" | grep -E "(spring-boot|spring-web|spring-data|mysql|jackson)" | head -20); do
    CLASSPATH="$CLASSPATH:$jar"
done

echo "📦 Compiling with basic classpath..."
cd backend/src/main/java

# Try to compile a simple class first
javac -cp "$CLASSPATH" com/example/bookstore/model/Book.java 2>&1 | head -10

echo ""
echo "🌐 Testing database connection..."
mysql -u root -e "SELECT 'Database connection: OK' as status, DATABASE() as current_db;" booksdb 2>/dev/null || echo "❌ Database connection failed"

echo ""
echo "📊 Testing simple REST endpoint manually..."
echo "We'll need to start the application to test the REST endpoints"

echo ""
echo "💡 Recommendations:"
echo "1. Fix Maven build issues first"
echo "2. Or try running with minimal configuration"
echo "3. Or use Docker for easier setup"