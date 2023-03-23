package com.matzip.server.global.auth.model;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(PARAMETER)
@Retention(RUNTIME)
@AuthenticationPrincipal(expression="userId == null ? 0L : userId")
public @interface CurrentUser {
}
