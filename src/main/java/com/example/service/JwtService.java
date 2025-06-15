package com.example.service;

import com.example.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Service pour la gestion des tokens JWT
 * Gère la génération et la vérification des tokens
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Extrait le nom d'utilisateur du token
     * @param token Token JWT
     * @return le nom d'utilisateur
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait une information du token, par exemple le nom d'utilisateur, l'expiration, le role, etc.
     * @param token Token JWT
     * @param claimsResolver Fonction pour extraire la valeur
     * @param <T> Type de la valeur extraite
     * @return la valeur extraite
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Génère un token JWT
     * @param user Utilisateur
     * @return le token JWT
     */
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", user.isAdmin() ? List.of("ROLE_ADMIN") : List.of("ROLE_USER"));
        return generateToken(extraClaims, user);
    }

    /**
     * Génère un token JWT avec des informations supplémentaires
     * @param extraClaims Informations supplémentaires
     * @param user Utilisateur
     * @return le token JWT
     */
    public String generateToken(Map<String, Object> extraClaims, User user) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Vérifie si un token JWT est valide
     * @param token Token JWT
     * @param userDetails Utilisateur
     * @return true si le token est valide, false sinon
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Vérifie si un token JWT est expiré
     * @param token Token JWT
     * @return true si le token est expiré, false sinon
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrait la date d'expiration d'un token JWT
     * @param token Token JWT
     * @return la date d'expiration
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait toutes les informations d'un token JWT
     * @param token Token JWT
     * @return les informations du token
     */
    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Récupère la clé de signature
     * @return la clé de signature
     */
    private Key getSignInKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 