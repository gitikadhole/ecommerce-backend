package com.example.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.example.ecommerce.entity.RefreshToken;
import com.example.ecommerce.entity.User;

import jakarta.transaction.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
	
	Optional<RefreshToken> findByToken(String token);
	
	@Modifying
	@Transactional
	void deleteByUser(User user);
	
	@Modifying
	@Transactional
	void deleteByUser(Long userId);
}
