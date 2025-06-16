import { describe, it, expect } from 'vitest';
import { mockBook, mockBooks } from '../test/mocks/mockData';

describe('Basic Test Suite', () => {
  it('should verify test environment is working', () => {
    expect(true).toBe(true);
  });

  it('should import mock data correctly', () => {
    expect(mockBook).toBeDefined();
    expect(mockBook.title).toBe('The Great Gatsby');
    expect(mockBooks).toHaveLength(3);
  });

  it('should handle basic math operations', () => {
    expect(2 + 2).toBe(4);
    expect(10 / 2).toBe(5);
  });

  it('should work with arrays', () => {
    const testArray = [1, 2, 3, 4, 5];
    expect(testArray).toHaveLength(5);
    expect(testArray[0]).toBe(1);
    expect(testArray.includes(3)).toBe(true);
  });

  it('should work with objects', () => {
    const testObject = {
      name: 'Test',
      value: 42,
      active: true
    };
    
    expect(testObject.name).toBe('Test');
    expect(testObject.value).toBe(42);
    expect(testObject.active).toBe(true);
  });
});