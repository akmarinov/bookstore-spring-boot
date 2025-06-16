# Bookstore Frontend

A modern React TypeScript frontend application for the Bookstore Spring Boot backend.

## Features

- **Modern React with TypeScript**: Built with Vite for fast development and hot module replacement
- **Material-UI**: Beautiful, responsive UI components with Material Design
- **React Router**: Client-side routing for seamless navigation
- **React Hook Form**: Efficient form handling with validation
- **Axios**: HTTP client for API communication
- **Custom Hooks**: Reusable logic for data fetching and state management
- **Responsive Design**: Mobile-first approach with responsive grid system
- **Error Handling**: Comprehensive error handling and loading states
- **Form Validation**: Client-side validation with Yup schema validation

## Project Structure

```
src/
├── api/                    # API service layer
│   ├── client.ts          # Axios configuration
│   └── bookApi.ts         # Book-related API calls
├── components/            # Reusable components
│   ├── BookForm.tsx       # Book form component
│   ├── BookItem.tsx       # Individual book display
│   ├── BookList.tsx       # Book list with pagination
│   └── Layout.tsx         # App layout wrapper
├── hooks/                 # Custom React hooks
│   ├── useBook.ts         # Single book operations
│   └── useBooks.ts        # Multiple books operations
├── pages/                 # Page components
│   ├── AddBookPage.tsx    # Add new book page
│   ├── EditBookPage.tsx   # Edit/view book page
│   └── HomePage.tsx       # Main page with book list
├── types/                 # TypeScript interfaces
│   └── Book.ts           # Book-related types
├── App.tsx               # Main application component
├── theme.ts              # Material-UI theme configuration
└── main.tsx              # Application entry point
```

## Getting Started

### Prerequisites

- Node.js (version 18 or higher)
- npm or yarn
- Bookstore Spring Boot backend running on port 8080

### Installation

1. Install dependencies:
```bash
npm install
```

2. Create environment configuration:
```bash
cp .env.example .env
```

3. Update the `.env` file with your backend API URL:
```
VITE_API_BASE_URL=http://localhost:8080/api
```

### Development

Start the development server:
```bash
npm run dev
```

The application will be available at `http://localhost:5173`

### Building for Production

Build the application:
```bash
npm run build
```

Preview the production build:
```bash
npm run preview
```

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run type-check` - Run TypeScript type checking

## API Integration

The frontend is designed to work with the Spring Boot backend REST API. The main endpoints used are:

- `GET /api/books` - Get all books with pagination
- `GET /api/books/{id}` - Get book by ID
- `POST /api/books` - Create new book
- `PUT /api/books/{id}` - Update book
- `DELETE /api/books/{id}` - Delete book
- `GET /api/books/search` - Search books
- `GET /api/books/category/{category}` - Get books by category

## Features Overview

### Book Management
- View all books in a responsive grid layout
- Add new books with comprehensive form validation
- Edit existing books
- Delete books with confirmation dialog
- Search books by title or author
- Filter books by category
- Pagination for large datasets

### User Interface
- Clean, modern Material Design interface
- Responsive design that works on desktop and mobile
- Loading states and error handling
- Form validation with helpful error messages
- Confirmation dialogs for destructive actions

### Technical Features
- TypeScript for type safety
- Custom hooks for data management
- Axios interceptors for request/response handling
- React Hook Form for efficient form handling
- Yup schema validation
- Material-UI theming and customization

## Browser Support

This application supports all modern browsers including:
- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Contributing

1. Follow the existing code style and patterns
2. Add TypeScript types for new features
3. Include proper error handling
4. Test your changes thoroughly
5. Update documentation as needed
