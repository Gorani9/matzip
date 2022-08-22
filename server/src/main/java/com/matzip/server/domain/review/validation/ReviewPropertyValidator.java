package com.matzip.server.domain.review.validation;

import com.matzip.server.domain.review.model.Review;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ReviewPropertyValidator implements ConstraintValidator<ReviewProperty, String> {
    private static final String nullMessage = "No Property is given.";
    private static final String violationMessage = "This property is not allowed to be used in sort.";

    @Override
    public void initialize(ReviewProperty constraintAnnotation) {}

    @Override
    public boolean isValid(String property, ConstraintValidatorContext context) {
        try {
            Review.class.getDeclaredField(property);
            if (!property.equals("rating")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(violationMessage).addConstraintViolation();
                return false;
            } else return true;
        } catch (NoSuchFieldException e) {
            if (!property.equals("createdAt")) {
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
