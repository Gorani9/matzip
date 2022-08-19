package com.matzip.server.domain.user.validation;

import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class PasswordValidator implements ConstraintValidator<Password, String> {
    private static final int MIN_SIZE = 8;
    private static final int MAX_SIZE = 50;
    private static final String regex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?([^\\w\\s]|_)).{" + MIN_SIZE + "," + MAX_SIZE + "}$";
    private static final String nullMessage = "No Password is given.";

    private static final String violationMessage = String.format(
            "Password must have at least %d, at most %d characters including at least one numeric, special character",
            MIN_SIZE, MAX_SIZE);

    @Override
    public void initialize(Password constraintAnnotation) {}

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(nullMessage).addConstraintViolation();
            return false;
        } else {
            boolean isValidPassword = password.matches(regex);
            if (!isValidPassword) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(violationMessage).addConstraintViolation();
            }
            return isValidPassword;
        }
    }
}
