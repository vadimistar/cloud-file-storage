package com.vadimistar.cloudfilestorage.services;

import com.vadimistar.cloudfilestorage.auth.service.UserService;
import com.vadimistar.cloudfilestorage.auth.dto.RegisterDto;
import com.vadimistar.cloudfilestorage.exceptions.PasswordMismatchException;
import com.vadimistar.cloudfilestorage.exceptions.UserAlreadyExistsException;
import com.vadimistar.cloudfilestorage.auth.repository.UserRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void beforeEach() {
        userRepository.deleteAll();
    }

    @Test
    public void register_ok_createsRecordInDatabase() {
        RegisterDto registerDto = RegisterDto.builder()
                .username("user")
                .email("user@mail.com")
                .password("password")
                .confirmPassword("password")
                .build();

        assertDoesNotThrow(() -> userService.registerUser(registerDto));
        assertEquals(1, userRepository.count());
    }

    @SneakyThrows
    @Test
    public void register_usernameAlreadyExists_throwsUserAlreadyExistsException() {
        RegisterDto user1 = RegisterDto.builder()
                .username("user")
                .email("user@mail.com")
                .password("password")
                .confirmPassword("password")
                .build();
        userService.registerUser(user1);

        RegisterDto user2 = RegisterDto.builder()
                .username("user")
                .email("user2@mail.com")
                .password("password")
                .confirmPassword("password")
                .build();
        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(user2));
    }

    @SneakyThrows
    @Test
    public void register_emailAlreadyExists_throwsUserAlreadyExistsException() {
        RegisterDto user1 = RegisterDto.builder()
                .username("user")
                .email("user@mail.com")
                .password("password")
                .confirmPassword("password")
                .build();
        userService.registerUser(user1);

        RegisterDto user2 = RegisterDto.builder()
                .username("user2")
                .email("user@mail.com")
                .password("password")
                .confirmPassword("password")
                .build();
        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(user2));
    }

    @Test
    public void register_passwordAndConfirmPasswordAreDifferent_throwsPasswordMismatchException() {
        RegisterDto user = RegisterDto.builder()
                .username("user")
                .email("user@mail.com")
                .password("password")
                .confirmPassword("different password")
                .build();
        assertThrows(PasswordMismatchException.class, () -> userService.registerUser(user));
    }

    @Container
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    public static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }
}
