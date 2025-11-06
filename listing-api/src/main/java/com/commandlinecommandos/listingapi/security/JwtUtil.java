package com.commandlinecommandos.listingapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret:myVerySecureSecretKeyForJWTTokensThatShouldBeAtLeast256BitsLongForHS256AlgorithmUpdated2024}")
    private String secret;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Long extractUserId(String token) {
        try {
            String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
            if (userIdStr == null) {
                return null;
            }
            
            // Try to parse as UUID and convert to Long
            try {
                UUID uuid = UUID.fromString(userIdStr);
                // Convert UUID to Long using most significant bits
                // This is a deterministic conversion
                return uuid.getMostSignificantBits() & Long.MAX_VALUE;
            } catch (IllegalArgumentException e) {
                // If not a UUID, try to parse as Long directly
                try {
                    return Long.parseLong(userIdStr);
                } catch (NumberFormatException ex) {
                    // If neither works, use hash code
                    return (long) userIdStr.hashCode();
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
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
    
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

