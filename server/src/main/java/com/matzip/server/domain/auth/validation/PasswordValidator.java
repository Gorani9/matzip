package com.matzip.server.domain.auth.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {
    private static final int MIN_SIZE = 8;
    private static final int MAX_SIZE = 30;
    private static final String regex =
            "^(?=.*?[a-zA-Z])(?=.*?[0-9])(?=.*?([^\\w\\s]|_)).{" + MIN_SIZE + "," + MAX_SIZE + "}$";
    private static final String nullMessage = "should not be null nor blank";

    private static final String violationMessage = String.format(
            "must have at least %d, at most %d characters including at least one alphabet, numeric, and special character",
            MIN_SIZE, MAX_SIZE);

    @Override
    public void initialize(Password constraintAnnotation) {}

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
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
