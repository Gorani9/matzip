package com.matzip.server.domain.user.dto;

import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class PasswordValidator implements ConstraintValidator<Password, String> {
    private static final int MIN_SIZE = 8;
    private static final int MAX_SIZE = 50;
    private static final String regexPassword = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?([^\\w\\s]|_)).{" +
            MIN_SIZE + "," + MAX_SIZE +
            "}$";
    private static final String violationMessage = String.format(
            "Password must have at least %d, at most %d characters including at least one numeric, special character",
            MIN_SIZE,
            MAX_SIZE
    );

    @Override
    public void initialize(Password constraintAnnotation) {
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        boolean isValidPassword = password.matches(regexPassword);
        if (!isValidPassword) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(violationMessage).addConstraintViolation();
        }
        return isValidPassword;
    }

    public boolean isValid(String password) {
        return password.matches(regexPassword);
    }
}
