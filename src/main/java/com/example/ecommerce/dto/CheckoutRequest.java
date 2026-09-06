package com.example.ecommerce.dto;

import lombok.Data;

@Data
public class CheckoutRequest {
	private String fullName;
	private String phone;
	private String street;
	private String city;
	private String state;
	private String zipCode;
	private String country;
}
