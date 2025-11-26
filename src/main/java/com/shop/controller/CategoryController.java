package com.shop.controller;

import com.shop.dto.CategoryDto;
import com.shop.service.CategoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class CategoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    
    private final CategoryService categoryService;
    
    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        logger.info("GET /api/categories - Retrieving all categories");
        List<CategoryDto> categories = categoryService.findAll();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        logger.info("GET /api/categories/{} - Retrieving category", id);
        CategoryDto category = categoryService.findById(id);
        return ResponseEntity.ok(category);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CategoryDto>> searchCategories(@RequestParam String keyword) {
        logger.info("GET /api/categories/search - Searching categories with keyword: {}", keyword);
        List<CategoryDto> categories = categoryService.findByKeyword(keyword);
        return ResponseEntity.ok(categories);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        logger.info("POST /api/categories - Creating new category: {}", categoryDto.getName());
        CategoryDto savedCategory = categoryService.save(categoryDto);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id, 
                                                     @Valid @RequestBody CategoryDto categoryDto) {
        logger.info("PUT /api/categories/{} - Updating category", id);
        CategoryDto updatedCategory = categoryService.update(id, categoryDto);
        return ResponseEntity.ok(updatedCategory);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        logger.info("DELETE /api/categories/{} - Deleting category", id);
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        logger.info("GET /api/categories/exists/{} - Checking if category exists", id);
        boolean exists = categoryService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}