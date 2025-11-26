package com.shop.mapper;

import com.shop.dto.UserDto;
import com.shop.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    
    UserDto toDto(User user);
    
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "orders", ignore = true)
    User toEntity(UserDto userDto);
    
    List<UserDto> toDtoList(List<User> users);
    
    List<User> toEntityList(List<UserDto> userDtos);
    
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "orders", ignore = true)
    void updateEntityFromDto(UserDto userDto, @MappingTarget User user);
}