package com.commandlinecommandos.campusmarketplace.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.UserRole;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret:myVerySecureSecretKeyForJWTTokensThatShouldBeAtLeast256BitsLongForHS256AlgorithmUpdated2024}")
    private String secret;
    
    @Value("${jwt.access-token.expiration:3600000}") // 1 hour
    private Long accessTokenExpiration;
    
    @Value("${jwt.refresh-token.expiration:604800000}") // 7 days
    private Long refreshTokenExpiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public UserRole extractRole(String token) {
        return UserRole.valueOf(extractClaim(token, claims -> claims.get("role", String.class)));
    }
    
    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getUserId() != null ? user.getUserId().toString() : null);
        claims.put("email", user.getEmail());
        return createToken(claims, user.getUsername(), accessTokenExpiration);
    }
    
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId() != null ? user.getUserId().toString() : null);
        claims.put("tokenType", "refresh");
        return createToken(claims, user.getUsername(), refreshTokenExpiration);
    }
    
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public Long getAccessTokenExpiration() {
        return accessTokenExpiration / 1000; // return in seconds
    }
    
    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration / 1000; // return in seconds
    }
}
