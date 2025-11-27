package com.shop.mapper;

import com.shop.dto.OrderDto;
import com.shop.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {
    
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "userName")
    @Mapping(target = "orderItems", ignore = true)
    OrderDto toDto(Order order);
    
    @Mapping(source = "userId", target = "user.id")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Order toEntity(OrderDto orderDto);
    
    List<OrderDto> toDtoList(List<Order> orders);
    
    List<Order> toEntityList(List<OrderDto> orderDtos);
    
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDto(OrderDto orderDto, @MappingTarget Order order);
}