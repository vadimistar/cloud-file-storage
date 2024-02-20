package com.vadimistar.cloudfilestorage.services;

import com.vadimistar.cloudfilestorage.dto.RegisterDto;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.exceptions.PasswordMismatchException;
import com.vadimistar.cloudfilestorage.exceptions.UserAlreadyExistsException;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {

    void registerUser(RegisterDto registerDto) throws UserAlreadyExistsException, PasswordMismatchException;

    Optional<User> getUserByUsername(String username);
}
