package com.vadimistar.cloudfilestorage.auth.service;

import com.vadimistar.cloudfilestorage.auth.dto.RegisterDto;
import com.vadimistar.cloudfilestorage.auth.exception.PasswordMismatchException;
import com.vadimistar.cloudfilestorage.auth.exception.UserAlreadyExistsException;
import com.vadimistar.cloudfilestorage.auth.repository.UserRepository;
import com.vadimistar.cloudfilestorage.common.TestUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
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
    public void registerUser_validRegisterDto_createsRecordInDatabase() {
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
    public void registerUser_usernameAlreadyExists_throwsUserAlreadyExistsException() {
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
    public void registerUser_emailAlreadyExists_throwsUserAlreadyExistsException() {
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
    public void registerUser_passwordAndConfirmPasswordAreDifferent_throwsPasswordMismatchException() {
        RegisterDto user = RegisterDto.builder()
                .username("user")
                .email("user@mail.com")
                .password("password")
                .confirmPassword("different password")
                .build();
        assertThrows(PasswordMismatchException.class, () -> userService.registerUser(user));
    }

    @Container
    private static final MySQLContainer<?> mysql = TestUtils.createMySqlContainer();

    @DynamicPropertySource
    public static void mysqlProperties(DynamicPropertyRegistry registry) {
        TestUtils.addMySqlProperties(registry, mysql);
    }

    @Container
    private static final MinIOContainer minio = TestUtils.createMinIOContainer();

    @DynamicPropertySource
    public static void minioProperties(DynamicPropertyRegistry registry) {
        TestUtils.addMinioProperties(registry, minio);
    }
}
