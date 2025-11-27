package com.shop.mapper;

import com.shop.dto.ProductDto;
import com.shop.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductDto toDto(Product product);
    
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Product toEntity(ProductDto productDto);
    
    List<ProductDto> toDtoList(List<Product> products);
    
    List<Product> toEntityList(List<ProductDto> productDtos);
    
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    void updateEntityFromDto(ProductDto productDto, @MappingTarget Product product);
}