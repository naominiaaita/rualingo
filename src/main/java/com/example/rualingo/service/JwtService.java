package com.example.rualingo.service;

import com.example.rualingo.config.AuthProperties;
import com.example.rualingo.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final Key signingKey;
    private final long expirationMs;

    public JwtService(AuthProperties authProperties) {
        String secret = authProperties.getJwt().getSecret();
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = authProperties.getJwt().getExpirationMs();
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
                .claim("role", roleName)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration() != null && claims.getExpiration().after(new Date());
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
