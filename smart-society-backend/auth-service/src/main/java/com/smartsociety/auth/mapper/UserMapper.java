package com.smartsociety.auth.mapper;

import com.smartsociety.auth.dto.request.RegisterRequest;
import com.smartsociety.auth.dto.response.UserResponse;
import com.smartsociety.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id",                   ignore = true)
    @Mapping(target = "passwordHash",         ignore = true)
    @Mapping(target = "status",               ignore = true)
    @Mapping(target = "failedLoginAttempts",  ignore = true)
    @Mapping(target = "lockedUntil",          ignore = true)
    @Mapping(target = "lastLoginAt",          ignore = true)
    @Mapping(target = "createdAt",            ignore = true)
    @Mapping(target = "updatedAt",            ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserResponse toUserResponse(User user);
}