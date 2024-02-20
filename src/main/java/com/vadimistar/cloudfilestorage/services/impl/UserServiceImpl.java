package com.vadimistar.cloudfilestorage.services.impl;

import com.vadimistar.cloudfilestorage.dto.RegisterDto;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.exceptions.PasswordMismatchException;
import com.vadimistar.cloudfilestorage.exceptions.UserAlreadyExistsException;
import com.vadimistar.cloudfilestorage.repositories.UserRepository;
import com.vadimistar.cloudfilestorage.config.UserDetailsImpl;
import com.vadimistar.cloudfilestorage.services.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        Optional<User> user = userRepository.getUserByUsername(username);
        return user.map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("User with this username is not found"));
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }
}
