package com.example.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class AddressResponse {
	private String fullName;
	private String phone;
	private String street;
	private String city;
	private String state;
	private String zipCode;
	private String country; 
}
