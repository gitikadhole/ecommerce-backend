package com.example.ecommerce.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
	
	public OpenAPI ecommerceOpenAPI() {
		return new OpenAPI().info(new Info().title("E-Commerce Backend API")
				.description("REST APIs for E-commerce backend platform").version("1.0"));
	}
}
