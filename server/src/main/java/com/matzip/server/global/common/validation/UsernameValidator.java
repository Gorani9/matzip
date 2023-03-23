package com.matzip.server.global.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UsernameValidator implements ConstraintValidator<Username, String> {
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 30;
    private static final String regex = "^(?!.*\\.\\.)(?!.*\\.$)[\\w.]{" + MIN_SIZE + "," + MAX_SIZE + "}$";
    private static final String nullMessage = "should not be null nor blank";
    private static final String reservedUsername = "'admin' is not allowed.";
    private static final String violationMessage = String.format(
            "must have at least %d, at most %d characters with alphabets, numerics, underscore, and '.'" +
            " ",
            MIN_SIZE, MAX_SIZE);

    @Override
    public void initialize(Username constraintAnnotation) {}

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.isBlank()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(nullMessage).addConstraintViolation();
            return false;
        } else if (username.equalsIgnoreCase("admin")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(reservedUsername).addConstraintViolation();
            return false;
        } else {
            boolean isValidUsername = username.matches(regex);
            if (!isValidUsername) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(violationMessage).addConstraintViolation();
            }
            return isValidUsername;
        }
    }
}
