package com.ecommerce.userservice.mapper;

import com.ecommerce.userservice.dto.response.UserResponse;
import com.ecommerce.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role", target = "role")
    @Mapping(source = "createdAt", target = "createdAt")
    UserResponse toUserResponse(User user);
}
