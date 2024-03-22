package com.vadimistar.cloudfilestorage.security.mapper;

import com.vadimistar.cloudfilestorage.security.dto.RegisterUserRequestDto;
import com.vadimistar.cloudfilestorage.security.entity.User;
import com.vadimistar.cloudfilestorage.security.dto.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserDto makeUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public User makeUser(RegisterUserRequestDto request) {
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
    }
}
