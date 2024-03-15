package com.vadimistar.cloudfilestorage.security.dto;

import com.vadimistar.cloudfilestorage.validation.ConfirmPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ConfirmPassword
public class RegisterUserRequestDto {

    @NotNull
    @Size(min = 4, max = 200, message = "Username must be between 4 and 200 characters")
    @Pattern(regexp = "^[A-Za-z0-9_]*$", message = "Username must contain only valid characters: A-Z, a-z, 0-9, _")
    private String username;

    @NotNull
    @Email(message = "Invalid email")
    private String email;

    @NotNull
    @Size(min = 4, max = 200, message = "Password must be between 4 and 200 characters")
    private String password;

    @NotNull
    private String confirmPassword;
}
