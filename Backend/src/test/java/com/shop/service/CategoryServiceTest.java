package com.shop.service;

import com.shop.dto.CategoryDto;
import com.shop.entity.Category;
import com.shop.exception.ResourceNotFoundException;
import com.shop.mapper.CategoryMapper;
import com.shop.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private CategoryMapper categoryMapper;
    
    @InjectMocks
    private CategoryService categoryService;
    
    private Category category;
    private CategoryDto categoryDto;
    
    @BeforeEach
    void setUp() {
        category = new Category("Electronics", "Electronic devices");
        category.setId(1L);
        
        categoryDto = new CategoryDto("Electronics", "Electronic devices");
        categoryDto.setId(1L);
    }
    
    @Test
    void findAll_ShouldReturnAllCategories() {
        // Given
        List<Category> categories = Arrays.asList(category);
        List<CategoryDto> categoryDtos = Arrays.asList(categoryDto);
        
        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toDtoList(categories)).thenReturn(categoryDtos);
        
        // When
        List<CategoryDto> result = categoryService.findAll();
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Electronics");
        verify(categoryRepository).findAll();
        verify(categoryMapper).toDtoList(categories);
    }
    
    @Test
    void findById_WhenCategoryExists_ShouldReturnCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);
        
        // When
        CategoryDto result = categoryService.findById(1L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Electronics");
        verify(categoryRepository).findById(1L);
        verify(categoryMapper).toDto(category);
    }
    
    @Test
    void findById_WhenCategoryDoesNotExist_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> categoryService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 1");
    }
    
    @Test
    void save_WhenNewCategory_ShouldReturnSavedCategory() {
        // Given
        CategoryDto newCategoryDto = new CategoryDto("Books", "Book category");
        Category newCategory = new Category("Books", "Book category");
        Category savedCategory = new Category("Books", "Book category");
        savedCategory.setId(2L);
        CategoryDto savedCategoryDto = new CategoryDto("Books", "Book category");
        savedCategoryDto.setId(2L);
        
        when(categoryRepository.existsByName("Books")).thenReturn(false);
        when(categoryMapper.toEntity(newCategoryDto)).thenReturn(newCategory);
        when(categoryRepository.save(newCategory)).thenReturn(savedCategory);
        when(categoryMapper.toDto(savedCategory)).thenReturn(savedCategoryDto);
        
        // When
        CategoryDto result = categoryService.save(newCategoryDto);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Books");
        verify(categoryRepository).save(newCategory);
    }
    
    @Test
    void save_WhenCategoryAlreadyExists_ShouldThrowException() {
        // Given
        CategoryDto newCategoryDto = new CategoryDto("Electronics", "Electronic devices");
        
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> categoryService.save(newCategoryDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category with name 'Electronics' already exists");
    }
    
    @Test
    void deleteById_WhenCategoryExists_ShouldDeleteCategory() {
        // Given
        when(categoryRepository.existsById(1L)).thenReturn(true);
        
        // When
        categoryService.deleteById(1L);
        
        // Then
        verify(categoryRepository).deleteById(1L);
    }
    
    @Test
    void deleteById_WhenCategoryDoesNotExist_ShouldThrowException() {
        // Given
        when(categoryRepository.existsById(anyLong())).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> categoryService.deleteById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 1");
    }
}