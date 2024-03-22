package com.vadimistar.cloudfilestorage.security.service.impl;

import com.vadimistar.cloudfilestorage.security.dto.RegisterUserRequestDto;
import com.vadimistar.cloudfilestorage.security.dto.UserDto;
import com.vadimistar.cloudfilestorage.security.entity.User;
import com.vadimistar.cloudfilestorage.security.exception.RegisterUserException;
import com.vadimistar.cloudfilestorage.security.mapper.UserMapper;
import com.vadimistar.cloudfilestorage.security.repository.UserRepository;
import com.vadimistar.cloudfilestorage.security.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto registerUser(RegisterUserRequestDto request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RegisterUserException("Passwords don't match");
        }
        Optional<User> existingUser = userRepository.getUserByUsernameOrEmail(request.getUsername(), request.getEmail());
        if (existingUser.isPresent()) {
            if (existingUser.get().getUsername().equals(request.getUsername())) {
                throw new RegisterUserException("User with this username already exists");
            }
            if (existingUser.get().getEmail().equals(request.getEmail())) {
                throw new RegisterUserException("User with this email already exists");
            }
        }
        User user = userMapper.makeUser(request);
        userRepository.save(user);
        return userMapper.makeUserDto(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.getUserByUsername(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        username,
                        user.getPassword(),
                        List.of(new SimpleGrantedAuthority("user"))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User with this username is not found"));
    }

    @Override
    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.getUserByUsername(username)
                .map(userMapper::makeUserDto);
    }
}
