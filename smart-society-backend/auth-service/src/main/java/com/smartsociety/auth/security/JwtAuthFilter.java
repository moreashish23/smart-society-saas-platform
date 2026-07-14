package com.smartsociety.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final SecretKey secretKey;

    public JwtAuthFilter(@Value("${app.jwt.secret}") String secret) {
        byte[] keyBytes = Base64.getEncoder().encode(secret.getBytes());
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(new String(keyBytes)));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {


        String userId    = request.getHeader("X-User-Id");
        String role      = request.getHeader("X-User-Role");
        String societyId = request.getHeader("X-Society-Id");

        if (userId != null && !userId.isBlank() && role != null && !role.isBlank()) {

            request.setAttribute("userId",    userId);
            request.setAttribute("role",      role);
            request.setAttribute("societyId", societyId);

            var auth = new UsernamePasswordAuthenticationToken(
                    userId, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            SecurityContextHolder.getContext().setAuthentication(auth);

        } else {

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Claims claims = Jwts.parser()
                            .verifyWith(secretKey)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

                    String subject = claims.getSubject();
                    String claimRole = claims.get("role", String.class);

                    request.setAttribute("userId", subject);
                    request.setAttribute("role",   claimRole);

                    Object sid = claims.get("societyId");
                    if (sid != null) request.setAttribute("societyId", sid.toString());

                    var auth = new UsernamePasswordAuthenticationToken(
                            subject, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + claimRole)));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                } catch (JwtException ex) {
                    log.debug("JWT validation failed: {}", ex.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}