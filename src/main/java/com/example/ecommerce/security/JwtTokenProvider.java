package com.example.ecommerce.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
	
	@Value("${jwt.secret}")
	private String jwtSecret;
	
	@Value("${jwt.expiration-ms}")
	private long jwtExpirationMs;
	
	private Key key() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
	}
	
	public String generateToken(Authentication authentication) {
		String username = authentication.getName();
		String role = authentication.getAuthorities().iterator().next().getAuthority();
		Date now = new Date();
		Date expiryDate = new Date(now.getTime()+jwtExpirationMs);
		
		return Jwts.builder()
				.setSubject(username)
				.claim("role", role)
				.setIssuedAt(now)
				.setExpiration(expiryDate)
				.signWith(key(),SignatureAlgorithm.HS256)
				.compact();
	}
	
	public String getUsernameFromToken(String token) {
		return Jwts.parserBuilder().setSigningKey(key()).build()
				.parseClaimsJws(token).getBody().getSubject();
	}
	
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key()).build().parse(token);
			return true;
			
		} catch(JwtException | IllegalArgumentException e) {
			return false;
		}
	}
}

