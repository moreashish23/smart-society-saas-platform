package com.smartsociety.notification.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${app.jwt.secret}") String jwtSecret) {
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(
                        java.util.Base64.getEncoder()
                                .encodeToString(jwtSecret.getBytes())));
    }

    public boolean isValid(String token) {
        try { parseClaims(token); return true; }
        catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage()); return false;
        }
    }

    public String getUserId(String token)   { return parseClaims(token).getSubject(); }
    public String getRole(String token)     { return parseClaims(token).get("role", String.class); }
    public String getSocietyId(String token){ return parseClaims(token).get("societyId", String.class); }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();
    }
}