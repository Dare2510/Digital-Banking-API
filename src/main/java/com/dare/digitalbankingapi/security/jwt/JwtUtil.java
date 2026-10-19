package com.dare.digitalbankingapi.security.jwt;

import com.dare.digitalbankingapi.user.entity.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

	@Value("${JWT_SECRET}")
	String jwtSecret;

	@Value("${JWT_EXIPIRATION_MS}")
	Long jwtExpirationMs;

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes());
	}

	public String generateToken(String email, UserRole role, Long userId) {
		return Jwts.builder().
				subject(email).
				claim("userRole", role.name()).
				claim("userId", userId).
				issuedAt(new Date(System.currentTimeMillis())).
				expiration(new Date(System.currentTimeMillis() + jwtExpirationMs)).
				signWith(getSigningKey()).
				compact();
	}

	public String extractEmail(String token) {
		return Jwts.parser().
				verifyWith((SecretKey) getSigningKey()).
				build().
				parseSignedClaims(token).
				getPayload().
				getSubject();
	}

	public UserRole extractUserRole(String token) {
		return UserRole.valueOf(Jwts.parser().
				verifyWith((SecretKey) getSigningKey()).
				build().
				parseSignedClaims(token).
				getPayload().
				get("userRole").
				toString());
	}

	public Long extractUserId(String token) {
		return Long.valueOf(Jwts.parser().
				verifyWith((SecretKey) getSigningKey()).
				build().
				parseSignedClaims(token).
				getPayload().
				get("userId").
				toString());
	}


}
