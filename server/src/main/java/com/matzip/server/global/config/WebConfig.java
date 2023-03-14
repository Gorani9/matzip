package com.matzip.server.global.config;

import com.matzip.server.domain.review.model.ReviewProperty;
import com.matzip.server.domain.user.model.UserProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(false);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new UserPropertyConverter());
        registry.addConverter(new ReviewPropertyConverter());
    }

    private static class UserPropertyConverter implements Converter<String, UserProperty> {
        @Override
        public UserProperty convert(String source) {
            return UserProperty.from(source);
        }
    }

    private static class ReviewPropertyConverter implements Converter<String, ReviewProperty> {
        @Override
        public ReviewProperty convert(String source) {
            return ReviewProperty.from(source);
        }
    }
}
