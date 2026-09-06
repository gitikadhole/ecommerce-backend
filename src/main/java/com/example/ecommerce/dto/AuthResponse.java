package com.example.ecommerce.dto;

import com.example.ecommerce.entity.Role;

public record AuthResponse(
		String email,
		String fullName,
		Role role,
		String message
		) {

}
