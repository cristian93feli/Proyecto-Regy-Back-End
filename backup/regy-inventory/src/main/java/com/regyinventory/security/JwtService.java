package com.regyinventory.security;

import com.regyinventory.entities.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(CustomUserDetails userDetails) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("id", userDetails.getUsuario().getId());
        claims.put("username", userDetails.getUsername());

        String nombreCompleto = userDetails.getUsuario().getNombre()
                + " "
                + userDetails.getUsuario().getApellido();

        claims.put("nombre", nombreCompleto.trim());

        List<String> roles = userDetails.getUsuario()
                .getRoles()
                .stream()
                .map(Rol::getNombre)
                .map(Enum::name)
                .sorted()
                .toList();

        claims.put("roles", roles);

        return buildToken(claims, userDetails.getUsername());
    }

    private String buildToken(
            Map<String, Object> claims,
            String username
    ) {
        Date issuedAt = new Date();
        Date expirationDate = new Date(
                issuedAt.getTime() + jwtExpiration
        );

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(issuedAt)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Long extractUserId(String token) {
        Number id = extractAllClaims(token).get("id", Number.class);

        return id != null ? id.longValue() : null;
    }

    public List<String> extractRoles(String token) {
        Object roles = extractAllClaims(token).get("roles");

        if (!(roles instanceof List<?> roleList)) {
            return List.of();
        }

        return roleList.stream()
                .map(String::valueOf)
                .toList();
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {
        String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && userDetails.isEnabled();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}