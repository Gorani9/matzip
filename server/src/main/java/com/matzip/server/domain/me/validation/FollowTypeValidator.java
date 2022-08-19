package com.matzip.server.domain.me.validation;

import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class FollowTypeValidator implements ConstraintValidator<FollowType, String> {
    private static final String violationMessage = "Follow Type must be 'follower' or 'following'.";

    @Override
    public void initialize(FollowType constraintAnnotation) {
    }

    @Override
    public boolean isValid(String type, ConstraintValidatorContext context) {
        if (!type.equals("follower") && !type.equals("following")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(violationMessage).addConstraintViolation();
            return false;
        } else return true;
    }
}
