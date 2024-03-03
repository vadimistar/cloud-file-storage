package com.vadimistar.cloudfilestorage.auth.validation;

import com.vadimistar.cloudfilestorage.auth.dto.RegisterDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ConfirmPasswordValidator implements ConstraintValidator<com.vadimistar.cloudfilestorage.auth.validation.ConfirmPassword, Object> {

    @Override
    public void initialize(com.vadimistar.cloudfilestorage.auth.validation.ConfirmPassword constraintAnnotation) {}

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        RegisterDto user = (RegisterDto) object;
        return user.getPassword().equals(user.getConfirmPassword());
    }
}
