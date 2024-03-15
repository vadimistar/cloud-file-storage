package com.vadimistar.cloudfilestorage.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotContainsSlashValidator implements ConstraintValidator<NotContainsSlash, String> {

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return !s.contains("/");
    }
}
