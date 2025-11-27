package com.shop.mapper;

import com.shop.dto.CategoryDto;
import com.shop.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {
    
    CategoryDto toDto(Category category);
    
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category toEntity(CategoryDto categoryDto);
    
    List<CategoryDto> toDtoList(List<Category> categories);
    
    List<Category> toEntityList(List<CategoryDto> categoryDtos);
    
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "products", ignore = true)
    void updateEntityFromDto(CategoryDto categoryDto, @MappingTarget Category category);
}