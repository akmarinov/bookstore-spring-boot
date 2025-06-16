import React from 'react';
import { useNavigate } from 'react-router-dom';
import type { BookFormData } from '../types/Book';
import { useBook } from '../hooks/useBook';
import BookForm from '../components/BookForm';

const AddBookPage: React.FC = () => {
  const navigate = useNavigate();
  const { createBook, loading, error } = useBook();

  const handleSubmit = async (data: BookFormData) => {
    try {
      await createBook(data);
      navigate('/');
    } catch (error) {
      // Error is handled by the useBook hook and displayed in the form
    }
  };

  const handleCancel = () => {
    navigate('/');
  };

  return (
    <BookForm
      title="Add New Book"
      onSubmit={handleSubmit}
      onCancel={handleCancel}
      loading={loading}
      error={error}
    />
  );
};

export default AddBookPage;