package com.vadimistar.cloudfilestorage.security.service.impl;

import com.vadimistar.cloudfilestorage.security.details.UserDetailsImpl;
import com.vadimistar.cloudfilestorage.security.dto.RegisterUserRequestDto;
import com.vadimistar.cloudfilestorage.security.entity.User;
import com.vadimistar.cloudfilestorage.security.exception.RegisterUserException;
import com.vadimistar.cloudfilestorage.security.mapper.UserMapper;
import com.vadimistar.cloudfilestorage.security.repository.UserRepository;
import com.vadimistar.cloudfilestorage.security.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void registerUser(RegisterUserRequestDto request) {
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
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.getUserByUsername(username)
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("Username is not found"));
    }
}
