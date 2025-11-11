package com.bers.services.mappers;

import com.bers.api.dtos.UserDtos.*;
import com.bers.domain.entities.User;
import com.bers.domain.entities.enums.UserStatus;
import org.mapstruct.*;


@Mapper(componentModel = "spring", imports = {UserStatus.class})
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(UserCreateRequest dto);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "status", source = "status")
    void updateEntity(UserUpdateRequest dto, @MappingTarget User user);

    @Mapping(target = "role", source = "role")
    @Mapping(target = "status", source = "status")
    UserResponse toResponse(User entity);
}