package com.order.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // Secret Key (Minimum 32 characters for HS256)
    @Value("${jwt.secret}")
    private String secret;

    // Create signing key
    private Key getSignKey() {

        return Keys.hmacShaKeyFor(secret.getBytes());

    }

    // Extract Username
    public String extractUsername(String token) {

        return extractClaim(token, Claims::getSubject);

    }

    //extract role
    public String extractRole(String token) {

        return extractClaim(token,
                claims -> claims.get("role", String.class));

    }

    // Extract Expiration Date
    public Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);

    }

    // Generic Claim Extractor
    public <T> T extractClaim(
            String token,
            Function<Claims, T> resolver) {

        Claims claims = extractAllClaims(token);

        return resolver.apply(claims);
    }

    // Extract all Claims
    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()

                .setSigningKey(getSignKey())

                .build()

                .parseClaimsJws(token)

                .getBody();
    }

    // Check Expiry
    private boolean isTokenExpired(String token) {

        return extractExpiration(token).before(new Date());

    }

    public boolean isTokenValid(String token) {

        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }


}