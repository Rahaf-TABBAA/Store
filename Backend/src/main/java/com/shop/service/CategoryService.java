package com.shop.service;

import com.shop.dto.CategoryDto;
import com.shop.entity.Category;
import com.shop.exception.ResourceNotFoundException;
import com.shop.mapper.CategoryMapper;
import com.shop.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    
    @Autowired
    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }
    
    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        logger.debug("Finding all categories");
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toDtoList(categories);
    }
    
    @Transactional(readOnly = true)
    public CategoryDto findById(Long id) {
        logger.debug("Finding category by id: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return categoryMapper.toDto(category);
    }
    
    @Transactional(readOnly = true)
    public CategoryDto findByName(String name) {
        logger.debug("Finding category by name: {}", name);
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
        return categoryMapper.toDto(category);
    }
    
    @Transactional(readOnly = true)
    public List<CategoryDto> findByKeyword(String keyword) {
        logger.debug("Finding categories by keyword: {}", keyword);
        List<Category> categories = categoryRepository.findByKeyword(keyword);
        return categoryMapper.toDtoList(categories);
    }
    
    public CategoryDto save(CategoryDto categoryDto) {
        logger.debug("Saving category: {}", categoryDto.getName());
        
        if (categoryDto.getId() == null && categoryRepository.existsByName(categoryDto.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDto.getName() + "' already exists");
        }
        
        Category category = categoryMapper.toEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        logger.info("Category saved successfully with id: {}", savedCategory.getId());
        
        return categoryMapper.toDto(savedCategory);
    }
    
    public CategoryDto update(Long id, CategoryDto categoryDto) {
        logger.debug("Updating category with id: {}", id);
        
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        if (!existingCategory.getName().equals(categoryDto.getName()) && 
            categoryRepository.existsByName(categoryDto.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDto.getName() + "' already exists");
        }
        
        categoryMapper.updateEntityFromDto(categoryDto, existingCategory);
        Category updatedCategory = categoryRepository.save(existingCategory);
        logger.info("Category updated successfully with id: {}", updatedCategory.getId());
        
        return categoryMapper.toDto(updatedCategory);
    }
    
    public void deleteById(Long id) {
        logger.debug("Deleting category with id: {}", id);
        
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        
        categoryRepository.deleteById(id);
        logger.info("Category deleted successfully with id: {}", id);
    }
    
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }
    
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}