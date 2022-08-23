package com.matzip.server.domain.review.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ReviewSearchTypeValidator implements ConstraintValidator<ReviewSearchType, String> {
    private static final String nullMessage = "No Property is given.";
    private static final String violationMessage = "This property is not allowed to be used in sort.";

    @Override
    public void initialize(ReviewSearchType constraintAnnotation) {}

    @Override
    public boolean isValid(String property, ConstraintValidatorContext context) {
        try {
            if (!property.equals("content") && !property.equals("location")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(violationMessage).addConstraintViolation();
                return false;
            } else return true;
        } catch (NullPointerException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(nullMessage).addConstraintViolation();
            return false;
        }
    }
}
