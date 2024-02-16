package com.vadimistar.cloudfilestorage.services;

import com.vadimistar.cloudfilestorage.dto.RegisterDto;
import com.vadimistar.cloudfilestorage.exceptions.PasswordMismatchException;
import com.vadimistar.cloudfilestorage.exceptions.UserAlreadyExistsException;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    void registerUser(RegisterDto registerDto) throws UserAlreadyExistsException, PasswordMismatchException;
}
