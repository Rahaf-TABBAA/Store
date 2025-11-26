package com.shop.mapper;

import com.shop.dto.OrderItemDto;
import com.shop.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderItemMapper {
    
    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderItemDto toDto(OrderItem orderItem);
    
    @Mapping(source = "orderId", target = "order.id")
    @Mapping(source = "productId", target = "product.id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    OrderItem toEntity(OrderItemDto orderItemDto);
    
    List<OrderItemDto> toDtoList(List<OrderItem> orderItems);
    
    List<OrderItem> toEntityList(List<OrderItemDto> orderItemDtos);
    
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDto(OrderItemDto orderItemDto, @MappingTarget OrderItem orderItem);
}