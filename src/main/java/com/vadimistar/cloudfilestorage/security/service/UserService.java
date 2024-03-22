package com.vadimistar.cloudfilestorage.security.service;

import com.vadimistar.cloudfilestorage.security.dto.RegisterUserRequestDto;
import com.vadimistar.cloudfilestorage.security.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {

    UserDto registerUser(RegisterUserRequestDto request);
    Optional<UserDto> getUserByUsername(String username);
}
