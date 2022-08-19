package com.matzip.server.domain.me.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotBlank;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy=FollowTypeValidator.class)
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
@Documented
@NotBlank
public @interface FollowType {
    String message() default "Invalid Follow Type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
