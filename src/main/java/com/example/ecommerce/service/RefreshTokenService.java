package com.example.ecommerce.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.ecommerce.entity.RefreshToken;
import com.example.ecommerce.repository.RefreshTokenRepository;
import com.example.ecommerce.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	
	@Value("${jwt.refresh-expiration-ms}")
	private long refreshTokenDurationMs;
	
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;
	
	@Transactional
	public RefreshToken createRefreshToken(String username) {
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(userRepository.findByEmail(username).orElseThrow());
		refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
		refreshToken.setToken(UUID.randomUUID().toString());
		
		refreshTokenRepository.deleteByUser(refreshToken.getUser());
		return refreshTokenRepository.save(refreshToken);
	}
	
	public Optional<RefreshToken> findByToken(String token){
		return refreshTokenRepository.findByToken(token);
	}
	
	public RefreshToken verifyExpiration(RefreshToken token) {
		if(token.getExpiryDate().compareTo(Instant.now())<0) {
			refreshTokenRepository.delete(token);
			throw new RuntimeException("Refresh Token Expired.Please login again");
		}
		return token;
	}

	@Transactional
	public void deleteByToken(String token) {
		// TODO Auto-generated method stub
		RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(()->new RuntimeException("Refresh Token not found"));
		
		refreshTokenRepository.delete(refreshToken);
		
		
	}
	
	@Transactional
	public void deleteByUserId(Long userId) {
		refreshTokenRepository.deleteByUser(userId);
	}

}
