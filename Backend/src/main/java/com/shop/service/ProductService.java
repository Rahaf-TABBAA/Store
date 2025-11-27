package com.shop.service;

import com.shop.dto.ProductDto;
import com.shop.entity.Category;
import com.shop.entity.Product;
import com.shop.exception.ResourceNotFoundException;
import com.shop.mapper.ProductMapper;
import com.shop.repository.CategoryRepository;
import com.shop.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    
    @Autowired
    public ProductService(ProductRepository productRepository, 
                         CategoryRepository categoryRepository,
                         ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }
    
    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        logger.debug("Finding all products");
        List<Product> products = productRepository.findAll();
        return productMapper.toDtoList(products);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductDto> findAll(Pageable pageable) {
        logger.debug("Finding all products with pagination");
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(productMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        logger.debug("Finding product by id: {}", id);
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toDto(product);
    }
    
    @Transactional(readOnly = true)
    public List<ProductDto> findByCategoryId(Long categoryId) {
        logger.debug("Finding products by category id: {}", categoryId);
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return productMapper.toDtoList(products);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductDto> findByCategoryId(Long categoryId, Pageable pageable) {
        logger.debug("Finding products by category id: {} with pagination", categoryId);
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return products.map(productMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public List<ProductDto> findActiveProducts() {
        logger.debug("Finding active products");
        List<Product> products = productRepository.findByIsActiveTrue();
        return productMapper.toDtoList(products);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductDto> findActiveProducts(Pageable pageable) {
        logger.debug("Finding active products with pagination");
        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        return products.map(productMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public List<ProductDto> findByKeyword(String keyword) {
        logger.debug("Finding products by keyword: {}", keyword);
        List<Product> products = productRepository.findByKeyword(keyword);
        return productMapper.toDtoList(products);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductDto> findByKeyword(String keyword, Pageable pageable) {
        logger.debug("Finding products by keyword: {} with pagination", keyword);
        Page<Product> products = productRepository.findByKeyword(keyword, pageable);
        return products.map(productMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public List<ProductDto> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        logger.debug("Finding products by price range: {} - {}", minPrice, maxPrice);
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return productMapper.toDtoList(products);
    }
    
    @Transactional(readOnly = true)
    public List<ProductDto> findLowStockProducts(Integer threshold) {
        logger.debug("Finding low stock products with threshold: {}", threshold);
        List<Product> products = productRepository.findLowStockProducts(threshold);
        return productMapper.toDtoList(products);
    }
    
    public ProductDto save(ProductDto productDto) {
        logger.debug("Saving product: {}", productDto.getName());
        
        if (productDto.getSku() != null && productRepository.existsBySku(productDto.getSku())) {
            throw new IllegalArgumentException("Product with SKU '" + productDto.getSku() + "' already exists");
        }
        
        Product product = productMapper.toEntity(productDto);
        
        // Set category if provided
        if (productDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDto.getCategoryId()));
            product.setCategory(category);
        }
        
        // Generate SKU if not provided
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            product.setSku(generateSKU());
        }
        
        Product savedProduct = productRepository.save(product);
        logger.info("Product saved successfully with id: {}", savedProduct.getId());
        
        return productMapper.toDto(savedProduct);
    }
    
    public ProductDto update(Long id, ProductDto productDto) {
        logger.debug("Updating product with id: {}", id);
        
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        if (productDto.getSku() != null && 
            !existingProduct.getSku().equals(productDto.getSku()) && 
            productRepository.existsBySku(productDto.getSku())) {
            throw new IllegalArgumentException("Product with SKU '" + productDto.getSku() + "' already exists");
        }
        
        productMapper.updateEntityFromDto(productDto, existingProduct);
        
        // Update category if provided
        if (productDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDto.getCategoryId()));
            existingProduct.setCategory(category);
        }
        
        Product updatedProduct = productRepository.save(existingProduct);
        logger.info("Product updated successfully with id: {}", updatedProduct.getId());
        
        return productMapper.toDto(updatedProduct);
    }
    
    public void deleteById(Long id) {
        logger.debug("Deleting product with id: {}", id);
        
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        
        productRepository.deleteById(id);
        logger.info("Product deleted successfully with id: {}", id);
    }
    
    public ProductDto updateStock(Long id, Integer quantity) {
        logger.debug("Updating stock for product id: {} to quantity: {}", id, quantity);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        product.setStockQuantity(quantity);
        Product updatedProduct = productRepository.save(product);
        logger.info("Stock updated successfully for product id: {}", id);
        
        return productMapper.toDto(updatedProduct);
    }
    
    public ProductDto increaseStock(Long id, Integer quantity) {
        logger.debug("Increasing stock for product id: {} by quantity: {}", id, quantity);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        product.increaseStock(quantity);
        Product updatedProduct = productRepository.save(product);
        logger.info("Stock increased successfully for product id: {}", id);
        
        return productMapper.toDto(updatedProduct);
    }
    
    public ProductDto decreaseStock(Long id, Integer quantity) {
        logger.debug("Decreasing stock for product id: {} by quantity: {}", id, quantity);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        product.decreaseStock(quantity);
        Product updatedProduct = productRepository.save(product);
        logger.info("Stock decreased successfully for product id: {}", id);
        
        return productMapper.toDto(updatedProduct);
    }
    
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }
    
    @Transactional(readOnly = true)
    public boolean existsBySku(String sku) {
        return productRepository.existsBySku(sku);
    }
    
    private String generateSKU() {
        return "PRD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}