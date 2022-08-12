package com.matzip.server.global.auth.jwt;

import com.matzip.server.global.auth.model.MatzipAuthenticationToken;
import com.matzip.server.global.auth.service.UserPrincipalDetailsService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Date;

@RequiredArgsConstructor
@Component
public final class JwtProvider {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserPrincipalDetailsService userPrincipalDetailsService;

    private final String tokenPrefix = "Bearer ";
    private final long refreshTokenValidTime = 2 * 7 * 24 * 60 * 60 * 1000L;
    @Value("${jwt-secret-key}")
    private String secretKey;

    @PostConstruct
    private void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    private String removeTokenPrefix(String tokenWithPrefix) {
        return tokenWithPrefix.replace(tokenPrefix, "").trim();
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(removeTokenPrefix(token)).getBody();
    }

    private String getSubjectFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Authentication getAuthenticationFromToken(String token) {
        UserDetails userDetails = userPrincipalDetailsService.loadUserByUsername(getSubjectFromToken(token));
        return new MatzipAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public String generateAccessToken(Authentication authentication) {
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        claims.put("roles", authentication.getAuthorities());
        Date now = new Date();
        long accessTokenValidTime = 2 * 60 * 60 * 1000L;
        return tokenPrefix + Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            logger.error("Token is not provided.");
            return false;
        } else if (!token.startsWith(tokenPrefix)) {
            logger.error("Token does not match type Bearer.");
            return false;
        }
        try {
            getClaimsFromToken(token);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature.");
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token.");
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token.");
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token.");
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }

    public String getHeader() {
        return "Authorization";
    }
}
