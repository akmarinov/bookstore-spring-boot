# Testing Guide - Bookstore Spring Boot Application

## Quick Start Testing

### Option 1: Docker (Recommended - Easiest)

1. **Start the complete application with Docker:**
```bash
cd /Users/andrey/Documents/Projects/JSP/bookstore-spring-boot
docker-compose up -d
```

2. **Wait 30-60 seconds for startup, then access:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api/v1/books
- API Documentation: http://localhost:8080/swagger-ui.html
- Database Admin: http://localhost:8082 (phpMyAdmin - root/password)

### Option 2: Manual Development Setup

1. **Start MySQL Database:**
```bash
cd /Users/andrey/Documents/Projects/JSP/bookstore-spring-boot
docker-compose up -d mysql
```

2. **Start Backend:**
```bash
cd backend
export JAVA_HOME="$(brew --prefix openjdk@17)" 2>/dev/null || export JAVA_HOME="$(brew --prefix openjdk@11)"
export PATH="$JAVA_HOME/bin:$PATH"
mvn spring-boot:run
```

3. **Start Frontend (in new terminal):**
```bash
cd frontend
npm install
npm run dev
```

4. **Access Applications:**
- Frontend: http://localhost:5173 (Vite dev server)
- Backend: http://localhost:8080

## Testing Steps

### 1. Test Backend API First

**Health Check:**
```bash
curl http://localhost:8080/actuator/health
```
Expected: `{"status":"UP"}`

**Get All Books:**
```bash
curl http://localhost:8080/api/v1/books
```

**Add a Book:**
```bash
curl -X POST http://localhost:8080/api/v1/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring Boot in Action",
    "author": "Craig Walls",
    "isbn": "978-1617292545",
    "price": 29.99,
    "description": "A comprehensive guide to Spring Boot",
    "category": "Technology",
    "publisher": "Manning",
    "publicationDate": "2023-01-01",
    "pages": 400,
    "stockQuantity": 10,
    "imageUrl": "https://example.com/spring-book.jpg"
  }'
```

### 2. Test Frontend Interface

1. **Open Frontend:** http://localhost:3000 (Docker) or http://localhost:5173 (Dev)

2. **Test Book List:**
   - Should see books displayed in cards
   - Test search functionality
   - Test category filtering
   - Test pagination (if you have many books)

3. **Test Add Book:**
   - Click "Add Book" button
   - Fill out the form
   - Submit and verify it appears in the list

4. **Test Edit/Delete:**
   - Click edit on a book
   - Modify some fields and save
   - Try deleting a book

### 3. Test API Documentation

Visit: http://localhost:8080/swagger-ui.html
- Try different endpoints interactively
- Test with different parameters

## Troubleshooting Common Issues

### Issue: Backend Won't Start

**Check Java Version:**
```bash
java -version
```
Should be Java 11 or 17.

**Check Port 8080:**
```bash
lsof -i :8080
```
Kill any existing process if needed.

**Check Database Connection:**
```bash
docker-compose logs mysql
```

### Issue: Frontend Won't Start

**Check Node Version:**
```bash
node --version
npm --version
```
Should be Node 16+ and npm 8+.

**Clear Cache:**
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

### Issue: API Calls Fail

**Check CORS in browser console:**
- Open browser dev tools (F12)
- Look for CORS errors in console
- Check Network tab for failed requests

**Test Backend Directly:**
```bash
curl -I http://localhost:8080/api/v1/books
```

### Issue: Database Connection Fails

**Check MySQL is Running:**
```bash
docker-compose ps
```

**Check Database Logs:**
```bash
docker-compose logs mysql
```

**Reset Database:**
```bash
docker-compose down -v
docker-compose up -d
```

## Quick Verification Script

Save this as `test-app.sh`:

```bash
#!/bin/bash
echo "🧪 Testing Bookstore Application..."

echo "📡 Testing Backend Health..."
curl -s http://localhost:8080/actuator/health | grep -q "UP" && echo "✅ Backend is healthy" || echo "❌ Backend is down"

echo "📚 Testing Books API..."
curl -s http://localhost:8080/api/v1/books | grep -q "\[\]" && echo "✅ Books API responding" || echo "❌ Books API failed"

echo "🌐 Testing Frontend..."
curl -s http://localhost:3000 | grep -q "<!DOCTYPE html>" && echo "✅ Frontend is serving" || echo "❌ Frontend is down"

echo "📊 Testing Swagger..."
curl -s http://localhost:8080/swagger-ui.html | grep -q "Swagger UI" && echo "✅ API docs available" || echo "❌ API docs failed"

echo "🎉 Testing complete!"
```

Make it executable and run:
```bash
chmod +x test-app.sh
./test-app.sh
```

## Performance Testing

**Test with multiple books:**
```bash
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/v1/books \
    -H "Content-Type: application/json" \
    -d "{\"title\":\"Book $i\",\"author\":\"Author $i\",\"isbn\":\"123456789$i\",\"price\":19.99}"
done
```

## Expected Results

After successful setup:
- ✅ Backend running on port 8080
- ✅ Frontend running on port 3000 (Docker) or 5173 (Dev)
- ✅ Database accessible
- ✅ All CRUD operations working
- ✅ API documentation accessible
- ✅ No CORS errors
- ✅ Books can be added, edited, deleted through UI
- ✅ Search and filtering work

## Next Steps

Once basic testing works:
1. Test with larger datasets
2. Test error scenarios (invalid data, network failures)
3. Test different browsers
4. Test mobile responsiveness
5. Run automated test suites (`npm test` in frontend, `mvn test` in backend)