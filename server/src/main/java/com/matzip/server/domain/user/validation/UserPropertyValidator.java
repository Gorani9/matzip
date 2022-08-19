package com.matzip.server.domain.user.validation;

import com.matzip.server.domain.user.model.User;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class UserPropertyValidator implements ConstraintValidator<UserProperty, String> {
    private static final String nullMessage = "No Property is given.";
    private static final String violationMessage = "This property is not allowed to be used in sort.";

    @Override
    public void initialize(UserProperty constraintAnnotation) {}

    @Override
    public boolean isValid(String property, ConstraintValidatorContext context) {
        try {
            User.class.getDeclaredField(property);
            if (!property.equals("username") && !property.equals("matzipLevel"))  {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(violationMessage).addConstraintViolation();
                return false;
            } else return true;
        } catch (NoSuchFieldException e) {
            if (!property.equals("createdAt"))  {
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
