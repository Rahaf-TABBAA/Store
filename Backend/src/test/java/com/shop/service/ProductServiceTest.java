package com.shop.service;

import com.shop.dto.ProductDto;
import com.shop.entity.Category;
import com.shop.entity.Product;
import com.shop.exception.ResourceNotFoundException;
import com.shop.mapper.ProductMapper;
import com.shop.repository.CategoryRepository;
import com.shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private ProductMapper productMapper;
    
    @InjectMocks
    private ProductService productService;
    
    private Product product;
    private ProductDto productDto;
    private Category category;
    
    @BeforeEach
    void setUp() {
        category = new Category("Electronics", "Electronic devices");
        category.setId(1L);
        
        product = new Product("Laptop", "Gaming laptop", new BigDecimal("1299.99"), 10);
        product.setId(1L);
        product.setCategory(category);
        product.setSku("LAPTOP-001");
        
        productDto = new ProductDto("Laptop", "Gaming laptop", new BigDecimal("1299.99"), 10);
        productDto.setId(1L);
        productDto.setCategoryId(1L);
        productDto.setSku("LAPTOP-001");
    }
    
    @Test
    void findById_WhenProductExists_ShouldReturnProduct() {
        // Given
        when(productRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(productDto);
        
        // When
        ProductDto result = productService.findById(1L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptop");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("1299.99"));
        verify(productRepository).findByIdWithCategory(1L);
        verify(productMapper).toDto(product);
    }
    
    @Test
    void findById_WhenProductDoesNotExist_ShouldThrowException() {
        // Given
        when(productRepository.findByIdWithCategory(anyLong())).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> productService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: 1");
    }
    
    @Test
    void save_WhenNewProductWithCategory_ShouldReturnSavedProduct() {
        // Given
        ProductDto newProductDto = new ProductDto("Mouse", "Gaming mouse", new BigDecimal("59.99"), 20);
        newProductDto.setCategoryId(1L);
        
        Product newProduct = new Product("Mouse", "Gaming mouse", new BigDecimal("59.99"), 20);
        Product savedProduct = new Product("Mouse", "Gaming mouse", new BigDecimal("59.99"), 20);
        savedProduct.setId(2L);
        savedProduct.setCategory(category);
        savedProduct.setSku("PRD-ABCD1234");
        
        ProductDto savedProductDto = new ProductDto("Mouse", "Gaming mouse", new BigDecimal("59.99"), 20);
        savedProductDto.setId(2L);
        savedProductDto.setCategoryId(1L);
        
        when(productMapper.toEntity(newProductDto)).thenReturn(newProduct);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.toDto(savedProduct)).thenReturn(savedProductDto);
        
        // When
        ProductDto result = productService.save(newProductDto);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Mouse");
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    void save_WhenCategoryNotFound_ShouldThrowException() {
        // Given
        ProductDto newProductDto = new ProductDto("Mouse", "Gaming mouse", new BigDecimal("59.99"), 20);
        newProductDto.setCategoryId(999L);
        
        Product newProduct = new Product("Mouse", "Gaming mouse", new BigDecimal("59.99"), 20);
        
        when(productMapper.toEntity(newProductDto)).thenReturn(newProduct);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> productService.save(newProductDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 999");
    }
    
    @Test
    void updateStock_WhenProductExists_ShouldUpdateStock() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productDto);
        
        // When
        ProductDto result = productService.updateStock(1L, 15);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(product.getStockQuantity()).isEqualTo(15);
        verify(productRepository).save(product);
    }
    
    @Test
    void increaseStock_WhenProductExists_ShouldIncreaseStock() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productDto);
        
        // When
        ProductDto result = productService.increaseStock(1L, 5);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(product.getStockQuantity()).isEqualTo(15); // 10 + 5
        verify(productRepository).save(product);
    }
    
    @Test
    void decreaseStock_WhenSufficientStock_ShouldDecreaseStock() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productDto);
        
        // When
        ProductDto result = productService.decreaseStock(1L, 3);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(product.getStockQuantity()).isEqualTo(7); // 10 - 3
        verify(productRepository).save(product);
    }
    
    @Test
    void decreaseStock_WhenInsufficientStock_ShouldThrowException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        // When & Then
        assertThatThrownBy(() -> productService.decreaseStock(1L, 15))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Insufficient stock");
    }
}