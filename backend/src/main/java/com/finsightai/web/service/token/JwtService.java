package com.finsightai.web.service.token;

import com.finsightai.web.exception.JwtKeyException;
import com.finsightai.web.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
public class JwtService {
    @Value("${testing.app.secret}")
    private String secret;

    @Value("${testing.app.lifetime}")
    private long lifetime;

    public String generateToken(User user) {
        return generateToken(user.getEmail());
    }

    public String generateToken(String email) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + lifetime);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(secretKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey secretKey() {
        try {
            byte[] keyBytes = MessageDigest.getInstance("SHA-512")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new JwtKeyException(exception);
        }
    }
}
