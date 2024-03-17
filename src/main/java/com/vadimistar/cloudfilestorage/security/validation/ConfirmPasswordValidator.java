package com.vadimistar.cloudfilestorage.security.validation;

import com.vadimistar.cloudfilestorage.security.dto.RegisterUserRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ConfirmPasswordValidator implements ConstraintValidator<ConfirmPassword, Object> {

    @Override
    public void initialize(ConfirmPassword constraintAnnotation) {}

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        RegisterUserRequestDto user = (RegisterUserRequestDto) object;
        return user.getPassword().equals(user.getConfirmPassword());
    }
}
