package com.smartsociety.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;

@Component
@Slf4j
public class GatewayJwtUtil {

    private final SecretKey secretKey;

    public GatewayJwtUtil(@Value("${app.jwt.secret}") String jwtSecret) {
        byte[] keyBytes = Base64.getEncoder().encode(jwtSecret.getBytes());
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(new String(keyBytes)));
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public String getSocietyId(String token) {
        Object raw = parseClaims(token).get("societyId");
        return raw != null ? raw.toString() : null;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}