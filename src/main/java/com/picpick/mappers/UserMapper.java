package com.picpick.mappers;

import com.picpick.dtos.UserResponse;
import com.picpick.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toDto(User user);

    User toEntity(UserResponse userResponse);
}
