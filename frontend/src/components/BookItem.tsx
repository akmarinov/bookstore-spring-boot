import React from 'react';
import {
  Card,
  CardContent,
  CardActions,
  Typography,
  Button,
  Chip,
  Box,
  CardMedia,
} from '@mui/material';
import { Edit, Delete, Visibility } from '@mui/icons-material';
import type { Book } from '../types/Book';

interface BookItemProps {
  book: Book;
  onEdit?: (book: Book) => void;
  onDelete?: (book: Book) => void;
  onView?: (book: Book) => void;
}

const BookItem: React.FC<BookItemProps> = ({ book, onEdit, onDelete, onView }) => {
  const handleEdit = () => {
    onEdit?.(book);
  };

  const handleDelete = () => {
    onDelete?.(book);
  };

  const handleView = () => {
    onView?.(book);
  };

  return (
    <Card 
      sx={{ 
        height: '100%', 
        display: 'flex', 
        flexDirection: 'column',
        minHeight: { xs: '360px', sm: '380px', md: '400px' },
        transition: 'all 0.3s ease-in-out'
      }}
    >
      {book.imageUrl && (
        <CardMedia
          component="img"
          height="200"
          image={book.imageUrl}
          alt={book.title}
          sx={{ 
            objectFit: 'cover',
            height: { xs: '160px', sm: '180px', md: '200px' }
          }}
        />
      )}
      <CardContent sx={{ flexGrow: 1, p: { xs: 2, sm: 2.5, md: 3 } }}>
        <Typography 
          gutterBottom 
          variant="h6" 
          component="div" 
          sx={{
            fontSize: { xs: '1.1rem', sm: '1.25rem', md: '1.35rem' },
            fontWeight: 600,
            lineHeight: 1.3,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
            minHeight: { xs: '2.6rem', sm: '2.8rem', md: '3rem' },
            mb: 1
          }}
          title={book.title}
        >
          {book.title}
        </Typography>
        <Typography 
          variant="subtitle2" 
          color="text.secondary" 
          gutterBottom
          sx={{
            fontSize: { xs: '0.8rem', sm: '0.875rem' },
            fontWeight: 500,
            mb: 1.5
          }}
        >
          by {book.author}
        </Typography>
        <Typography 
          variant="body2" 
          color="text.secondary" 
          sx={{ 
            mb: 1.5,
            fontSize: { xs: '0.75rem', sm: '0.8rem' }
          }}
        >
          ISBN: {book.isbn}
        </Typography>
        <Box sx={{ 
          display: 'flex', 
          alignItems: 'center', 
          gap: 1, 
          mb: 1.5,
          flexWrap: 'wrap'
        }}>
          <Chip
            label={book.category}
            color="primary"
            size="small"
            variant="outlined"
            sx={{ fontSize: { xs: '0.7rem', sm: '0.75rem' } }}
          />
          <Chip
            label={book.inStock ? 'In Stock' : 'Out of Stock'}
            color={book.inStock ? 'success' : 'error'}
            size="small"
            sx={{ fontSize: { xs: '0.7rem', sm: '0.75rem' } }}
          />
        </Box>
        <Typography 
          variant="h6" 
          color="primary" 
          sx={{ 
            fontWeight: 'bold',
            fontSize: { xs: '1.1rem', sm: '1.25rem' },
            mb: 1
          }}
        >
          ${book.price.toFixed(2)}
        </Typography>
        {book.description && (
          <Typography
            variant="body2"
            color="text.secondary"
            sx={{
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              display: '-webkit-box',
              WebkitLineClamp: { xs: 2, md: 3 },
              WebkitBoxOrient: 'vertical',
              fontSize: { xs: '0.8rem', sm: '0.875rem' },
              lineHeight: 1.4
            }}
          >
            {book.description}
          </Typography>
        )}
      </CardContent>
      <CardActions sx={{ 
        justifyContent: 'space-between', 
        px: { xs: 2, sm: 2.5, md: 3 }, 
        pb: { xs: 2, sm: 2.5, md: 3 },
        pt: 1,
        flexWrap: { xs: 'wrap', sm: 'nowrap' },
        gap: 1
      }}>
        <Button
          size="small"
          startIcon={<Visibility />}
          onClick={handleView}
          sx={{ 
            fontSize: { xs: '0.75rem', sm: '0.8rem' },
            minWidth: { xs: '70px', sm: '80px' }
          }}
        >
          View
        </Button>
        <Box sx={{ 
          display: 'flex', 
          gap: 0.5,
          flexWrap: { xs: 'wrap', sm: 'nowrap' }
        }}>
          <Button
            size="small"
            startIcon={<Edit />}
            onClick={handleEdit}
            sx={{ 
              fontSize: { xs: '0.75rem', sm: '0.8rem' },
              minWidth: { xs: '65px', sm: '75px' }
            }}
          >
            Edit
          </Button>
          <Button
            size="small"
            startIcon={<Delete />}
            onClick={handleDelete}
            color="error"
            sx={{ 
              fontSize: { xs: '0.75rem', sm: '0.8rem' },
              minWidth: { xs: '75px', sm: '85px' }
            }}
          >
            Delete
          </Button>
        </Box>
      </CardActions>
    </Card>
  );
};

export default BookItem;