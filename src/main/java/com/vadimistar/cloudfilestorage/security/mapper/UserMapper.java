package com.vadimistar.cloudfilestorage.security.mapper;

import com.vadimistar.cloudfilestorage.security.entity.User;
import com.vadimistar.cloudfilestorage.security.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto makeUserDto(User user) {
        return UserDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
