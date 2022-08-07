package com.matzip.server.global.config;

import com.matzip.server.global.auth.filter.JwtAuthenticationFilter;
import com.matzip.server.global.auth.filter.MatzipAuthenticationFilter;
import com.matzip.server.global.auth.jwt.JwtAuthenticationEntryPoint;
import com.matzip.server.global.auth.jwt.JwtProvider;
import com.matzip.server.global.auth.service.UserPrincipalDetailsService;
import com.matzip.server.global.common.exception.MatzipAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final MatzipAccessDeniedHandler matzipAccessDeniedHandler;
    private final UserPrincipalDetailsService userPrincipalDetailsService;

    private final String[] GET_WHITELIST = new String[] {
            "/ping/",
            "/api/v1/users/duplicate/",
    };

    private final String[] POST_WHITELIST = new String[] {
            "/api/v1/users/",
            "/api/v1/users/login/",
    };

    private final String[] CORS_WHITELIST = new String[] {
            "http://localhost:3000",
            "https://zippy-heliotrope-6168d0.netlify.app",
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(matzipAccessDeniedHandler)
                .and()
                .addFilter(new MatzipAuthenticationFilter(authenticationManager(), jwtProvider))
                .addFilter(new JwtAuthenticationFilter(authenticationManager(), jwtProvider))
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, GET_WHITELIST).permitAll()
                .antMatchers(HttpMethod.POST, POST_WHITELIST).permitAll()
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

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userPrincipalDetailsService);
        return daoAuthenticationProvider;
    }
}
