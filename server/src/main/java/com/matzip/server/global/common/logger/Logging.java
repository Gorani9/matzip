package com.matzip.server.global.common.logger;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(METHOD)
@Retention(RUNTIME)
public @interface Logging {
    String endpoint();
    boolean pathVariable() default false;
    boolean hideRequestBody() default false;
}
