package com.vadimistar.cloudfilestorage.security.service;

import com.vadimistar.cloudfilestorage.security.dto.RegisterUserRequestDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    void registerUser(RegisterUserRequestDto request);
}
