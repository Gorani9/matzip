package com.matzip.server.global.auth.service;

import com.matzip.server.domain.record.repository.LoginRecordRepository;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.auth.exception.InvalidJwtException;
import com.matzip.server.global.auth.model.MatzipAuthenticationToken;
import com.matzip.server.global.auth.model.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final UserPrincipalDetailsService userPrincipalDetailsService;
    private final LoginRecordRepository loginRecordRepository;

    @Value("${jwt.secret}")
    private String SECRET;

    private final static String PREFIX = "Bearer ";
    private final static Integer EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 2;
    private final static User anonymousUser = new User("anonymousUser", "");

    public String generateToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);

        claims.setIssuedAt(new Date());
        claims.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME));

        return PREFIX + Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS256, SECRET).compact();
    }

    public Authentication getAuthentication(String token) {
        if (token == null || token.isBlank()) {
            return new MatzipAuthenticationToken(new UserPrincipal(anonymousUser));
        } else if (!token.startsWith(PREFIX)) {
            throw new InvalidJwtException();
        }

        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(PREFIX, "")).getBody();
        } catch (Exception e) {
            throw new InvalidJwtException();
        }

        String username = claims.getSubject();

        UserPrincipal userDetails = (UserPrincipal) userPrincipalDetailsService.loadUserByUsername(username);
        loginRecordRepository.findById(userDetails.getUserId()).ifPresentOrElse( loginRecord -> {
            if (!loginRecord.getToken().equals(token)) throw new InvalidJwtException();
        }, () -> {
            log.error("LoginRecord not found for user: " + userDetails.getUsername());
            throw new InvalidJwtException();
        });

        return new MatzipAuthenticationToken(userDetails);
    }
}
