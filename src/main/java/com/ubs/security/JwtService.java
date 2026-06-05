package com.ubs.security;

import com.ubs.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

	private static final String ROLES_CLAIM = "roles";
	private static final String TOKEN_TYPE_CLAIM = "type";
	private static final String ACCESS_TOKEN_TYPE = "ACCESS";
	private static final String REFRESH_TOKEN_TYPE = "REFRESH";

	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateAccessToken(UserDetails userDetails) {
		return buildToken(userDetails, jwtProperties.expirationMs(), ACCESS_TOKEN_TYPE);
	}

	public String generateRefreshToken(UserDetails userDetails) {
		return buildToken(userDetails, jwtProperties.refreshExpirationMs(), REFRESH_TOKEN_TYPE);
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public List<String> extractRoles(String token) {
		return extractClaim(token, claims -> claims.get(ROLES_CLAIM, List.class));
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		try {
			Claims claims = extractAllClaims(token);
			return ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))
					&& userDetails.getUsername().equals(claims.getSubject())
					&& claims.getExpiration().after(new Date());
		} catch (JwtException ex) {
			return false;
		}
	}

	public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
		try {
			Claims claims = extractAllClaims(token);
			return REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))
					&& userDetails.getUsername().equals(claims.getSubject())
					&& claims.getExpiration().after(new Date());
		} catch (JwtException ex) {
			return false;
		}
	}

	public long getAccessTokenExpirationMs() {
		return jwtProperties.expirationMs();
	}

	private String buildToken(UserDetails userDetails, long expirationMs, String tokenType) {
		List<String> roles = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.toList();

		Date now = new Date();
		Date expiry = new Date(now.getTime() + expirationMs);

		return Jwts.builder()
				.subject(userDetails.getUsername())
				.claim(ROLES_CLAIM, roles)
				.claim(TOKEN_TYPE_CLAIM, tokenType)
				.issuedAt(now)
				.expiration(expiry)
				.signWith(secretKey)
				.compact();
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		try {
			return Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
		} catch (JwtException ex) {
			throw new InvalidTokenException("Invalid or expired JWT token", ex);
		}
	}

}
