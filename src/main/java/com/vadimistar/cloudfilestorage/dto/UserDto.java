package com.vadimistar.cloudfilestorage.dto;

import com.vadimistar.cloudfilestorage.validation.ConfirmPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@ConfirmPassword
public class UserDto {

    @NotNull
    @Size(min = 4, max = 200, message = "Username must be between 4 and 200 characters")
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
