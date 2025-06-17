import React, { useState, useEffect } from 'react';
import {
  Grid,
  Typography,
  Box,
  Pagination,
  Alert,
  CircularProgress,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
} from '@mui/material';
import { Search, FilterList } from '@mui/icons-material';
import type { Book } from '../types/Book';
import { useBooks } from '../hooks/useBooks';
import { useBook } from '../hooks/useBook';
import BookItem from './BookItem';
import { useNavigate } from 'react-router-dom';

interface BookListProps {
  initialCategory?: string;
  initialSearch?: string;
}

const BookList: React.FC<BookListProps> = ({ initialCategory, initialSearch }) => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState(initialSearch || '');
  const [selectedCategory, setSelectedCategory] = useState(initialCategory || '');
  const [currentPage, setCurrentPage] = useState(1);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [bookToDelete, setBookToDelete] = useState<Book | null>(null);

  const { deleteBook } = useBook();
  
  const {
    books,
    loading,
    error,
    totalPages,
    fetchBooks,
    refetch,
  } = useBooks();

  // Fetch books when filter parameters change
  useEffect(() => {
    fetchBooks({
      page: currentPage - 1,
      size: 12,
      category: selectedCategory || undefined,
      search: searchTerm || undefined,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage, selectedCategory, searchTerm]);

  const handleSearch = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(event.target.value);
    setCurrentPage(1);
  };

  const handleCategoryChange = (event: any) => {
    setSelectedCategory(event.target.value);
    setCurrentPage(1);
  };

  const handlePageChange = (_: React.ChangeEvent<unknown>, page: number) => {
    setCurrentPage(page);
  };

  const handleEdit = (book: Book) => {
    navigate(`/edit-book/${book.id}`);
  };

  const handleView = (book: Book) => {
    // For now, just navigate to edit page for viewing
    navigate(`/edit-book/${book.id}?view=true`);
  };

  const handleDeleteClick = (book: Book) => {
    setBookToDelete(book);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (bookToDelete?.id) {
      try {
        await deleteBook(bookToDelete.id);
        setDeleteDialogOpen(false);
        setBookToDelete(null);
        refetch();
      } catch (error) {
        console.error('Failed to delete book:', error);
      }
    }
  };

  const handleDeleteCancel = () => {
    setDeleteDialogOpen(false);
    setBookToDelete(null);
  };

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

  if (loading && books.length === 0) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Typography 
        variant="h4" 
        component="h1" 
        gutterBottom
        sx={{
          textAlign: { xs: 'center', md: 'left' },
          mb: { xs: 2, sm: 3, md: 4 },
          fontWeight: 700
        }}
      >
        Book Collection
      </Typography>

      {/* Search and Filter Controls */}
      <Paper sx={{ 
        p: { xs: 2, sm: 3, md: 4 }, 
        mb: { xs: 3, sm: 4, md: 5 },
        borderRadius: 2
      }}>
        <Grid container spacing={{ xs: 2, sm: 3 }} alignItems="center">
          <Grid item xs={12} md={8} lg={9}>
            <TextField
              fullWidth
              placeholder="Search books by title or author..."
              value={searchTerm}
              onChange={handleSearch}
              size="medium"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Search />
                  </InputAdornment>
                ),
              }}
              sx={{
                '& .MuiOutlinedInput-root': {
                  borderRadius: 2
                }
              }}
            />
          </Grid>
          <Grid item xs={12} md={4} lg={3}>
            <FormControl fullWidth>
              <InputLabel id="category-filter-label">Category</InputLabel>
              <Select
                value={selectedCategory}
                labelId="category-filter-label"
                id="category-filter-select"
                label="Category"
                onChange={handleCategoryChange}
                startAdornment={<FilterList sx={{ mr: 1 }} />}
                size="medium"
                sx={{
                  borderRadius: 2
                }}
              >
                <MenuItem value="">All Categories</MenuItem>
                {categories.map((category) => (
                  <MenuItem key={category} value={category}>
                    {category}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Paper>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {books.length === 0 && !loading && (
        <Alert severity="info">
          No books found. {searchTerm || selectedCategory ? 'Try adjusting your search criteria.' : 'Add some books to get started!'}
        </Alert>
      )}

      {/* Books Grid */}
      <Grid 
        container 
        spacing={{ xs: 2, sm: 3, md: 4 }}
        sx={{
          justifyContent: { xs: 'center', sm: 'flex-start' }
        }}
      >
        {books.map((book) => (
          <Grid 
            item 
            xs={12} 
            sm={6} 
            md={4} 
            lg={3}
            xl={2.4}
            key={book.id}
            sx={{
              display: 'flex',
              justifyContent: 'center'
            }}
          >
            <Box sx={{ width: '100%', maxWidth: { xs: '400px', sm: 'none' } }}>
              <BookItem
                book={book}
                onEdit={handleEdit}
                onDelete={handleDeleteClick}
                onView={handleView}
              />
            </Box>
          </Grid>
        ))}
      </Grid>

      {/* Pagination */}
      {totalPages > 1 && (
        <Box sx={{ 
          display: 'flex', 
          justifyContent: 'center', 
          mt: { xs: 4, sm: 5, md: 6 },
          mb: { xs: 2, sm: 3 }
        }}>
          <Pagination
            count={totalPages}
            page={currentPage}
            onChange={handlePageChange}
            color="primary"
            size="large"
            sx={{
              '& .MuiPaginationItem-root': {
                fontSize: { xs: '0.875rem', sm: '1rem' },
                minWidth: { xs: '32px', sm: '40px' },
                height: { xs: '32px', sm: '40px' }
              }
            }}
          />
        </Box>
      )}

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={handleDeleteCancel}>
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete "{bookToDelete?.title}"? This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleDeleteCancel}>Cancel</Button>
          <Button onClick={handleDeleteConfirm} color="error" variant="contained">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default BookList;