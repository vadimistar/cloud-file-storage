package com.vadimistar.cloudfilestorage.validation;

import com.vadimistar.cloudfilestorage.dto.RegisterDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ConfirmPasswordValidator implements ConstraintValidator<ConfirmPassword, Object> {

    @Override
    public void initialize(ConfirmPassword constraintAnnotation) {}

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        RegisterDto user = (RegisterDto) object;
        return user.getPassword().equals(user.getConfirmPassword());
    }
}
