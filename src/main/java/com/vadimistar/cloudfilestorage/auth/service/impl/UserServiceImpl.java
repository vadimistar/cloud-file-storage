package com.vadimistar.cloudfilestorage.auth.service.impl;

import com.vadimistar.cloudfilestorage.auth.entity.User;
import com.vadimistar.cloudfilestorage.auth.repository.UserRepository;
import com.vadimistar.cloudfilestorage.auth.service.UserService;
import com.vadimistar.cloudfilestorage.auth.dto.RegisterDto;
import com.vadimistar.cloudfilestorage.auth.exception.PasswordMismatchException;
import com.vadimistar.cloudfilestorage.auth.exception.UserAlreadyExistsException;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void registerUser(RegisterDto registerDto) throws UserAlreadyExistsException, PasswordMismatchException {
        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords don't match");
        }

        Optional<User> existingUser = userRepository.getUserByUsernameOrEmail(registerDto.getUsername(), registerDto.getEmail());

        if (existingUser.isPresent()) {
            if (existingUser.get().getUsername().equals(registerDto.getUsername())) {
                throw new UserAlreadyExistsException("User with this username already exists");
            }

            if (existingUser.get().getEmail().equals(registerDto.getEmail())) {
                throw new UserAlreadyExistsException("User with this email already exists");
            }
        }

        User user = User.builder()
                .username(registerDto.getUsername())
                .email(registerDto.getEmail())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .build();

        userRepository.save(user);
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
    public Optional<User> getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }
}
