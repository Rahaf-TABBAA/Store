package com.shop.controller;

import com.shop.dto.ProductDto;
import com.shop.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class ProductController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    private final ProductService productService;
    
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/products - Retrieving all products with pagination");
        Page<ProductDto> products = productService.findAll(pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<ProductDto>> getAllProductsList() {
        logger.info("GET /api/products/all - Retrieving all products as list");
        List<ProductDto> products = productService.findAll();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/active")
    public ResponseEntity<Page<ProductDto>> getActiveProducts(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/products/active - Retrieving active products");
        Page<ProductDto> products = productService.findActiveProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        logger.info("GET /api/products/{} - Retrieving product", id);
        ProductDto product = productService.findById(id);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductDto>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/products/category/{} - Retrieving products by category", categoryId);
        Page<ProductDto> products = productService.findByCategoryId(categoryId, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> searchProducts(
            @RequestParam String keyword,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/products/search - Searching products with keyword: {}", keyword);
        Page<ProductDto> products = productService.findByKeyword(keyword, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/price-range")
    public ResponseEntity<List<ProductDto>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        logger.info("GET /api/products/price-range - Retrieving products by price range: {} - {}", minPrice, maxPrice);
        List<ProductDto> products = productService.findByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductDto>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        logger.info("GET /api/products/low-stock - Retrieving low stock products with threshold: {}", threshold);
        List<ProductDto> products = productService.findLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        logger.info("POST /api/products - Creating new product: {}", productDto.getName());
        ProductDto savedProduct = productService.save(productDto);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, 
                                                   @Valid @RequestBody ProductDto productDto) {
        logger.info("PUT /api/products/{} - Updating product", id);
        ProductDto updatedProduct = productService.update(id, productDto);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> updateStock(@PathVariable Long id, 
                                                 @RequestBody Map<String, Integer> request) {
        logger.info("PATCH /api/products/{}/stock - Updating product stock", id);
        Integer quantity = request.get("quantity");
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity is required");
        }
        ProductDto updatedProduct = productService.updateStock(id, quantity);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @PatchMapping("/{id}/stock/increase")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> increaseStock(@PathVariable Long id, 
                                                   @RequestBody Map<String, Integer> request) {
        logger.info("PATCH /api/products/{}/stock/increase - Increasing product stock", id);
        Integer quantity = request.get("quantity");
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity is required");
        }
        ProductDto updatedProduct = productService.increaseStock(id, quantity);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @PatchMapping("/{id}/stock/decrease")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> decreaseStock(@PathVariable Long id, 
                                                   @RequestBody Map<String, Integer> request) {
        logger.info("PATCH /api/products/{}/stock/decrease - Decreasing product stock", id);
        Integer quantity = request.get("quantity");
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity is required");
        }
        ProductDto updatedProduct = productService.decreaseStock(id, quantity);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.info("DELETE /api/products/{} - Deleting product", id);
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        logger.info("GET /api/products/exists/{} - Checking if product exists", id);
        boolean exists = productService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}