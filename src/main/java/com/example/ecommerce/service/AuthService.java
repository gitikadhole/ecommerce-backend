package com.example.ecommerce.service;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.ecommerce.dto.AuthResponse;
import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepo;
	private final PasswordEncoder encoder;
	
	
	public AuthResponse register(RegisterRequest req) {
		
		if(userRepo.existsByEmail(req.email())) {
			throw new RuntimeException("Email Already Exists");
		}
		
		User user = User.builder()
				.email(req.email())
				.password(encoder.encode(req.password()))
				.fullName(req.fullName())
				.role(Role.CUSTOMER)
				.enabled(true)
				.build();
		
		userRepo.save(user);
		
		return new AuthResponse(user.getEmail(),user.getFullName(),user.getRole(),"Registered Successfully");
	}
	
	public AuthResponse login(LoginRequest req) {
		User user = userRepo.findByEmail(req.email())
				.orElseThrow(()-> new RuntimeException("Invalid Credentials"));
		
		if(!encoder.matches(req.password(), user.getPassword())) {
			throw new RuntimeException("Invalid Credentials");
		}
		
		return new AuthResponse(user.getEmail(),user.getFullName(),user.getRole(),"Login Successfully");
	}
}
