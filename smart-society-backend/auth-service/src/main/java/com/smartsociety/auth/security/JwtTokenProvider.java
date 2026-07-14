package com.smartsociety.auth.security;

import com.smartsociety.auth.config.AppProperties;
import com.smartsociety.auth.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long      accessTokenExpirationMs;

    public JwtTokenProvider(AppProperties appProperties) {
        String secret = appProperties.getJwt().getSecret();
        byte[] keyBytes = Base64.getEncoder().encode(secret.getBytes());
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(new String(keyBytes)));
        this.accessTokenExpirationMs = appProperties.getJwt().getAccessTokenExpirationMs();
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role",      user.getRole().name());
        claims.put("email",     user.getEmail());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName",  user.getLastName());

        if (user.getSocietyId() != null) {
            claims.put("societyId", user.getSocietyId().toString());
        }

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(secretKey)
                .compact();
    }
}