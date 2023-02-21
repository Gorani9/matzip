package com.matzip.server.global.config;

import com.matzip.server.global.auth.MatzipAccessDeniedHandler;
import com.matzip.server.global.auth.MatzipAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
    private final MatzipAuthenticationEntryPoint matzipAuthenticationEntryPoint;
    private final MatzipAccessDeniedHandler matzipAccessDeniedHandler;

    private final String[] GET_WHITELIST = new String[]{"/ping", "/signup/exists",};

    private final String[] POST_WHITELIST = new String[]{"/signup", "/login", "/admin/api/v1/login"};

    private final String[] CORS_WHITELIST = new String[]{"http://localhost:5173"};

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .logout().disable()
                .csrf().disable()
                .sessionManagement()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(matzipAuthenticationEntryPoint)
                .accessDeniedHandler(matzipAccessDeniedHandler)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, GET_WHITELIST).permitAll()
                .antMatchers(HttpMethod.POST, POST_WHITELIST).permitAll()
                .antMatchers("/admin/api/v1/**").hasAnyAuthority("ADMIN")
                .antMatchers("/api/v1/**").hasAnyAuthority("USER")
                .anyRequest().authenticated();
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
        config.addExposedHeader("Set-Cookie");

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
