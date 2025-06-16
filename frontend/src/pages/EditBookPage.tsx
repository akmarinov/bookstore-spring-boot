import React, { useEffect } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import type { BookFormData } from '../types/Book';
import { useBook } from '../hooks/useBook';
import BookForm from '../components/BookForm';
import { Box, Alert, CircularProgress } from '@mui/material';

const EditBookPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const isViewMode = searchParams.get('view') === 'true';
  
  const bookId = id ? parseInt(id, 10) : undefined;
  const { book, loading, error, fetchBook, updateBook } = useBook();

  useEffect(() => {
    if (bookId) {
      fetchBook(bookId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [bookId]);

  const handleSubmit = async (data: BookFormData) => {
    if (!bookId) return;
    
    try {
      await updateBook(bookId, data);
      navigate('/');
    } catch (error) {
      // Error is handled by the useBook hook and displayed in the form
    }
  };

  const handleCancel = () => {
    navigate('/');
  };

  if (!bookId) {
    return (
      <Alert severity="error">
        Invalid book ID. Please go back and try again.
      </Alert>
    );
  }

  if (loading && !book) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error && !book) {
    return (
      <Alert severity="error">
        {error}
      </Alert>
    );
  }

  if (!book) {
    return (
      <Alert severity="error">
        Book not found.
      </Alert>
    );
  }

  return (
    <BookForm
      book={book}
      title={isViewMode ? `View Book: ${book.title}` : `Edit Book: ${book.title}`}
      onSubmit={handleSubmit}
      onCancel={handleCancel}
      loading={loading}
      error={error}
      isViewMode={isViewMode}
    />
  );
};

export default EditBookPage;