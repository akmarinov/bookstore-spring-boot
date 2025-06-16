import React, { useEffect } from 'react';
import {
  Paper,
  TextField,
  Button,
  Grid,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Switch,
  Box,
  Alert,
  CircularProgress,
} from '@mui/material';
import { Save, Cancel } from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import type { Book, BookFormData } from '../types/Book';

interface BookFormProps {
  book?: Book;
  onSubmit: (data: BookFormData) => Promise<void>;
  onCancel: () => void;
  loading?: boolean;
  error?: string | null;
  title: string;
  isViewMode?: boolean;
}

const bookSchema = yup.object().shape({
  title: yup.string().required('Title is required').max(255, 'Title is too long'),
  author: yup.string().required('Author is required').max(255, 'Author name is too long'),
  isbn: yup
    .string()
    .required('ISBN is required')
    .matches(/^[\d-]+$/, 'ISBN should contain only digits and hyphens')
    .min(10, 'ISBN should be at least 10 characters')
    .max(17, 'ISBN should not exceed 17 characters'),
  price: yup
    .number()
    .required('Price is required')
    .positive('Price must be positive')
    .max(9999.99, 'Price is too high'),
  category: yup.string().required('Category is required'),
  description: yup.string().optional().max(1000, 'Description is too long'),
  imageUrl: yup.string().optional().url('Must be a valid URL'),
  inStock: yup.boolean().required(),
});

const categories = [
  'Fiction',
  'Non-Fiction',
  'Science',
  'Technology',
  'History',
  'Biography',
  'Mystery',
  'Romance',
  'Fantasy',
  'Educational',
];

const BookForm: React.FC<BookFormProps> = ({
  book,
  onSubmit,
  onCancel,
  loading = false,
  error,
  title,
  isViewMode = false,
}) => {
  const {
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<BookFormData>({
    resolver: yupResolver(bookSchema) as any,
    defaultValues: {
      title: '',
      author: '',
      isbn: '',
      price: 0,
      category: '',
      description: '',
      imageUrl: '',
      inStock: true,
    },
  });

  useEffect(() => {
    if (book) {
      reset({
        title: book.title,
        author: book.author,
        isbn: book.isbn,
        price: book.price,
        category: book.category,
        description: book.description || '',
        imageUrl: book.imageUrl || '',
        inStock: book.inStock,
      });
    }
  }, [book, reset]);

  const handleFormSubmit = async (data: BookFormData) => {
    try {
      await onSubmit(data);
    } catch (error) {
      // Error handling is done in the parent component
    }
  };

  return (
    <Paper sx={{ 
      p: { xs: 2, sm: 3, md: 4 },
      borderRadius: 2,
      maxWidth: '900px',
      mx: 'auto'
    }}>
      <Typography 
        variant="h5" 
        component="h2" 
        gutterBottom
        sx={{
          textAlign: { xs: 'center', md: 'left' },
          mb: { xs: 2, sm: 3 },
          fontSize: { xs: '1.5rem', sm: '1.75rem', md: '2rem' },
          fontWeight: 600
        }}
      >
        {title}
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Box component="form" onSubmit={handleSubmit(handleFormSubmit)}>
        <Grid container spacing={{ xs: 2, sm: 3, md: 4 }}>
          <Grid item xs={12} md={6}>
            <Controller
              name="title"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="Title"
                  error={!!errors.title}
                  helperText={errors.title?.message}
                  disabled={loading || isViewMode}
                />
              )}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <Controller
              name="author"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="Author"
                  error={!!errors.author}
                  helperText={errors.author?.message}
                  disabled={loading || isViewMode}
                />
              )}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <Controller
              name="isbn"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="ISBN"
                  error={!!errors.isbn}
                  helperText={errors.isbn?.message}
                  disabled={loading || isViewMode}
                />
              )}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <Controller
              name="price"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="Price"
                  type="number"
                  inputProps={{ step: 0.01, min: 0 }}
                  error={!!errors.price}
                  helperText={errors.price?.message}
                  disabled={loading || isViewMode}
                />
              )}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <Controller
              name="category"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth error={!!errors.category}>
                  <InputLabel>Category</InputLabel>
                  <Select
                    {...field}
                    label="Category"
                    disabled={loading || isViewMode}
                  >
                    {categories.map((category) => (
                      <MenuItem key={category} value={category}>
                        {category}
                      </MenuItem>
                    ))}
                  </Select>
                  {errors.category && (
                    <Typography variant="caption" color="error" sx={{ mt: 1, ml: 2 }}>
                      {errors.category.message}
                    </Typography>
                  )}
                </FormControl>
              )}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <Controller
              name="imageUrl"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="Image URL (optional)"
                  error={!!errors.imageUrl}
                  helperText={errors.imageUrl?.message}
                  disabled={loading || isViewMode}
                />
              )}
            />
          </Grid>

          <Grid item xs={12}>
            <Controller
              name="description"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="Description (optional)"
                  multiline
                  rows={4}
                  error={!!errors.description}
                  helperText={errors.description?.message}
                  disabled={loading || isViewMode}
                />
              )}
            />
          </Grid>

          <Grid item xs={12}>
            <Controller
              name="inStock"
              control={control}
              render={({ field }) => (
                <FormControlLabel
                  control={
                    <Switch
                      {...field}
                      checked={field.value}
                      disabled={loading || isViewMode}
                    />
                  }
                  label="In Stock"
                />
              )}
            />
          </Grid>

          <Grid item xs={12}>
            <Box sx={{ 
              display: 'flex', 
              gap: { xs: 1, sm: 2 }, 
              justifyContent: { xs: 'center', md: 'flex-end' },
              flexDirection: { xs: 'column', sm: 'row' },
              mt: { xs: 2, sm: 3 }
            }}>
              <Button
                variant="outlined"
                startIcon={<Cancel />}
                onClick={onCancel}
                disabled={loading}
                size="large"
                sx={{
                  minWidth: { xs: '120px', sm: '140px' },
                  order: { xs: 2, sm: 1 }
                }}
              >
                {isViewMode ? 'Back' : 'Cancel'}
              </Button>
              {!isViewMode && (
                <Button
                  type="submit"
                  variant="contained"
                  startIcon={loading ? <CircularProgress size={20} /> : <Save />}
                  disabled={loading || isSubmitting}
                  size="large"
                  sx={{
                    minWidth: { xs: '120px', sm: '160px' },
                    order: { xs: 1, sm: 2 }
                  }}
                >
                  {loading ? 'Saving...' : book ? 'Update Book' : 'Create Book'}
                </Button>
              )}
            </Box>
          </Grid>
        </Grid>
      </Box>
    </Paper>
  );
};

export default BookForm;