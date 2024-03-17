package com.vadimistar.cloudfilestorage.folder.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotContainsSlashValidator.class)
@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotContainsSlash {
    String message() default "Cannot contain slash";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
