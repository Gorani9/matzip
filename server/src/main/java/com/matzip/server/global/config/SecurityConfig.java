package com.matzip.server.global.config;

import com.matzip.server.global.auth.filter.MatzipExceptionHandler;
import com.matzip.server.global.auth.filter.MatzipFilter;
import com.matzip.server.global.auth.service.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final MatzipExceptionHandler matzipExceptionHandler;
    private final JwtProvider jwtProvider;

    private final String[] GET_WHITELIST = new String[]{
            "/api/v1/reviews",
            "/api/v1/reviews/**",
            "/api/v1/users",
            "/api/v1/users/**",
    };

    private final String[] POST_WHITELIST = new String[]{"/api/v1/auth/signup", "/api/v1/auth/login"};

    private final String[] CORS_WHITELIST = new String[]{"http://localhost:5173", "https://matzip.vercel.app"};

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .logout().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(matzipExceptionHandler)
                .accessDeniedHandler(matzipExceptionHandler)
                .and()
                .addFilter(new MatzipFilter(noAuthenticationManager(), jwtProvider))
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, GET_WHITELIST).hasAnyAuthority("USER", "ANONYMOUS")
                .antMatchers(HttpMethod.POST, POST_WHITELIST).hasAnyAuthority("USER", "ANONYMOUS")
                .antMatchers("/api/v1/**").hasAnyAuthority("USER")
                .anyRequest().denyAll();
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Arrays.asList(CORS_WHITELIST));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager noAuthenticationManager() {
        return authentication -> {
            throw new IllegalStateException("Default authentication is disabled.");
        };
    }
}
