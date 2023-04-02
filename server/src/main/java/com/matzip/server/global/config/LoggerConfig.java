package com.matzip.server.global.config;

import com.matzip.server.global.auth.model.UserPrincipal;
import com.matzip.server.global.common.exception.MatzipException;
import com.matzip.server.global.common.logger.Logging;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Aspect
@Configuration
public class LoggerConfig {
    @Around("execution(* com.matzip.server..*Controller.*(..)) && @annotation(logging)")
    public Object logMethod(ProceedingJoinPoint joinPoint, Logging logging) throws Throwable {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();

        String endpoint = logging.endpoint();
        int pathVariableIndex = -1;
        if (logging.pathVariable()) {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();

            for (int i = 0; i < parameterAnnotations.length; i++) {
                for (Annotation annotation : parameterAnnotations[i]) {
                    if (annotation.annotationType() == PathVariable.class) {
                        pathVariableIndex = i;
                        break;
                    }
                }
            }

            String pathVariable = parameterValues[pathVariableIndex].toString();
            endpoint = endpoint.replace("{pathVariable}", pathVariable);
        }

        int finalPathVariableIndex = pathVariableIndex;
        String parameters = logging.hideRequestBody() ? "\t requestBody = [hidden]" :
                            IntStream.range(2, parameterNames.length)
                                    .filter(i -> i != finalPathVariableIndex)
                                    .mapToObj(i -> "\t " + parameterNames[i] + " = " + parameterValues[i])
                                    .collect(Collectors.joining("\n"));
        String endpointWithParameters = endpoint + (parameters.isEmpty() ? "" : "\n" + parameters);

        String ip = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserIp();
        String userId = (Long) parameterValues[0] == 0 ? ip : parameterValues[0].toString();

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long end = System.currentTimeMillis();
            log.info("[{}(id={})] {}\n took {}ms", parameterValues[1], userId, endpointWithParameters, end - start);
            return result;
        } catch (Exception e) {
            long end = System.currentTimeMillis();
            log.info("[{}(id={})] {}\n\t failed with {}\n took {}ms",
                     parameterValues[1], userId,
                     endpointWithParameters,
                     e.getMessage(), end - start);

            if (!(e instanceof MatzipException)) {
                e.printStackTrace();
            }

            throw e;
        }
    }
}
