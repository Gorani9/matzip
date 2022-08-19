package com.matzip.server.domain.user.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy=UserPropertyValidator.class)
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
@Documented
public @interface UserProperty {
    String message() default "Invalid User Property";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
