package com.bookmyshow.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    
    @Value("${bookmyshow.security.jwt.secret}")
    private String secret;
    
    @Value("${bookmyshow.security.jwt.issuer}")
    private String issuer;
    
    @Value("${bookmyshow.security.jwt.audience}")
    private String audience;
    
    @Value("${bookmyshow.security.jwt.access-token-expiry-minutes}")
    private long accessTokenExpiryMinutes;
    
    @Value("${bookmyshow.security.jwt.refresh-token-expiry-days}")
    private long refreshTokenExpiryDays;
    
    private SecretKey secretKey;
    
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, accessTokenExpiryMinutes * 60 * 1000, "access");
    }
    
    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, refreshTokenExpiryDays * 24 * 60 * 60 * 1000, "refresh");
    }
    
    private String generateToken(UserDetails userDetails, long expiryMillis, String type) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expiryMillis);
        
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .id(UUID.randomUUID().toString())
                .claim("type", type)
                .claim("roles", userDetails.getAuthorities())
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }
    
    public boolean validateToken(String token, UserDetails userDetails) {
        String userId = extractClaim(token, Claims::getSubject);
        return userId.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
    
    public String getUserIdFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public String getTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }
    
    public Instant getExpiration(String token) {
        return extractClaim(token, Claims::getExpiration).toInstant();
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    private boolean isTokenExpired(String token) {
        return getExpiration(token).isBefore(Instant.now());
    }
}